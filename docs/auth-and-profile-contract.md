# Auth and profile contract (frontend)

This document is the **contract between archimedes-backend and the frontend** for authentication, role selection, profile completion, and personal info. Use it when implementing the UI so requests/responses and flow stay in sync.

**Base URL:** All auth endpoints live under `{API_BASE}/api/v1/auth` (e.g. `https://api.example.com/api/v1/auth`).

**Authentication:** Protected endpoints require the header:

```http
Authorization: Bearer <access_token_or_id_token>
```

Use the token returned after login (password or OAuth callback). The backend validates Cognito-issued JWTs.

---

## 1. High-level flow

1. **Login** – User signs in via:
   - **Google (OAuth):** Redirect to backend OAuth URL → Cognito/Google consent → backend callback redirects to frontend `/auth/callback#access_token=...` (when `FRONTEND_URL` is set).
   - **Password:** `POST /api/v1/auth/login` with `username` and `password`; response includes tokens and `user` info.

2. **Check profile** – Frontend calls `GET /api/v1/auth/me` with `Authorization: Bearer <token>`.

3. **Branch:**
   - If **`profile` is not null** → User has a linked student or teacher (or is admin). Use `user_type` and `profile` to show the correct app.
   - If **`profile` is null** and **`user_type` is not `"admin"`** → Show **“Complete your profile”**: choose role (Student / Teacher), then context (join code for students, school for teachers), then submit.

4. **Complete profile** – Frontend calls `POST /api/v1/auth/complete-profile` with body (see below). **Send the ID token** in the `Authorization` header (not the access token), or the backend returns "Email is required to complete profile". Backend creates the student or teacher and returns the same shape as `/me`.

5. **Personal info (optional)** – After profile exists, user can update name via `PATCH /api/v1/auth/me`.

**Roles in the UI:** Show only **Student** and **Teacher**. **Admin** is never a choice; the backend returns `user_type: "admin"` for users configured as admin (e.g. by email or Cognito id in backend config).

---

## 2. Endpoints reference

### 2.1 Google (OAuth) sign-in

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/v1/auth/oauth/url` | No | Returns `{ "url": "<Cognito Hosted UI URL>", "redirect_uri": "..." }`. Frontend redirects user to `url` (e.g. `window.location = url`). |
| GET | `/api/v1/auth/oauth/redirect` | No | 302 redirect to Cognito Hosted UI. Use as the href for a “Sign in with Google” button. |
| GET | `/api/v1/auth/callback?code=...&state=...` | No | OAuth callback. Backend exchanges `code` for tokens. **Callback URL** must match `OAUTH_CALLBACK_URI` in your backend .env (e.g. `http://localhost:8001/api/v1/auth/callback` in Cognito). |

**Query params (optional):**

- `oauth/url`: `state`, `identity_provider` (default `"Google"`).
- `oauth/redirect`: same.
- `callback`: `code` (required), `state` (optional).

**OAuth callback behavior (custom auth UI):**

When `FRONTEND_URL` is configured, the backend **redirects** (302) the browser to:

```
{FRONTEND_URL}/auth/callback#access_token=...&refresh_token=...&id_token=...&token_type=Bearer&expires_in=...
```

- Tokens are in the URL **fragment** (not sent to the server; standard OAuth SPA pattern).
- Fragment params: `access_token` (required), `refresh_token` (optional), `id_token` (optional), `token_type` (optional), `expires_in` (optional).
- **Store `id_token`** as well as `access_token`: the backend needs the **ID token** for `POST /auth/complete-profile` (see below).
- On error (e.g. `invalid_scope`), redirect: `{FRONTEND_URL}/auth/callback#error=...&error_description=...`
- When `FRONTEND_URL` is empty, backend returns HTML or JSON (legacy Hosted UI fallback).

---

### 2.2 Password login

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/auth/login` | No | Body: `{ "username": string, "password": string }`. Returns tokens and user info. |

**Response (200):**

```json
{
  "access_token": "string",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "string | null",
  "id_token": "string | null",
  "user": {
    "username": "string",
    "email": "string | null",
    "given_name": "string | null",
    "family_name": "string | null",
    "sub": "string | null"
  }
}
```

Store `access_token` (and optionally `refresh_token`, `id_token`) for subsequent requests.

---

### 2.3 Current user (me)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/v1/auth/me` | Bearer | Returns the current user’s role and, if linked, their student or teacher profile. |
| PATCH | `/api/v1/auth/me` | Bearer | Update current user’s personal info (e.g. first name, last name). |

**GET /me response (200):**

```json
{
  "user_type": "students" | "teachers" | "admin" | null,
  "profile": { ... } | null
}
```

- **`user_type`**
  - `"students"` – User has a linked **student** record; `profile` is the student object.
  - `"teachers"` – User has a linked **teacher** record; `profile` is the teacher object.
  - `"admin"` – User is an admin (backend-configured); `profile` is typically `null`. Show admin UI; do not show “Complete profile”.
  - `null` – No linked profile; show “Complete your profile” (role + context).
- **`profile`** – When present, a **student** or **teacher** object (see profile shape below). For admin, usually `null`.

**PATCH /me request body:** Optional fields (camelCase or snake_case per backend schema).

```json
{
  "firstName": "string (optional)",
  "lastName": "string (optional)"
}
```

**PATCH /me response (200):** Same as GET /me (`user_type` and updated `profile`).

**Errors:**

- 401 – Missing or invalid token.
- 404 (PATCH only) – No profile linked; user must complete profile first.

---

### 2.4 Complete profile

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/auth/complete-profile` | Bearer **id_token** | Create the linked student or teacher record. **Use the ID token** in `Authorization: Bearer <id_token>` (not the access token). The backend reads email/name from the token; Cognito puts these only in the ID token. Idempotent: if profile exists, returns 200 with existing profile. |

**Request body (camelCase):**

- **`userType`** (required): `"students"` | `"teachers"`.
- **Students – choose one:**
  - **`joinCode`** (optional): Class join code (5–10 chars). Backend resolves the course, uses its school, creates the student, and enrolls them in that class. Do not send `schoolId` when using `joinCode`.
  - **`schoolId`** (optional): UUID of the school. Creates the student at that school with no class enrollment.
  - Validation: student must have **exactly one** of `joinCode` or `schoolId` (not both, not neither).
- **Teachers:**
  - **`schoolId`** (required): UUID of the school.

**Examples:**

```json
{ "userType": "students", "joinCode": "AB12X" }
{ "userType": "students", "schoolId": "550e8400-e29b-41d4-a716-446655440000" }
{ "userType": "teachers", "schoolId": "550e8400-e29b-41d4-a716-446655440000" }
```

**Response:**

- **201** – Profile created; body same as GET /me (`user_type` and `profile`).
- **200** – Profile already existed (idempotent); body same as GET /me.

**Errors:**

- 400 – Validation (e.g. student with both or neither of `joinCode`/`schoolId`, teacher without `schoolId`, email missing when backend needs it from token).
- 404 – Invalid join code or course not found.
- 401 – Missing or invalid token.

---

### 2.5 Token refresh and logout

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/auth/refresh` | No | Body: `{ "refresh_token": "string" }`. Returns new access token (and optionally id_token). |
| POST | `/api/v1/auth/logout` | No | Body: `{ "access_token": "string" }`. Invalidates the token. |

---

### 2.6 Email/password registration (optional path)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/auth/register` | No | Body: `username`, `email`, `password`, `givenName`, `familyName`, `schoolId`, `userType` (`"students"` \| `"teachers"`). Creates Cognito user and student/teacher in one step. Not used for OAuth users. |

---

## 3. Profile shape (student / teacher)

When `profile` is not null in GET /me or complete-profile response, it is either a **student** or **teacher** object.

**Student** (when `user_type === "students"`):

- `id`, `school_id`, `first_name`, `last_name`, `full_name`, `email`, `username`, `is_active`, `created_at`, `updated_at`, and optionally `cognito_user_id`.

**Teacher** (when `user_type === "teachers"`):

- `id`, `school_id`, `first_name`, `last_name`, `full_name`, `email`, `username`, `max_classes`, `is_active`, `created_at`, `updated_at`, and optionally `cognito_user_id`.

UUIDs are strings; timestamps are ISO 8601. Frontend can rely on these fields for display and for calling other APIs (e.g. student id for enrollments).

---

## 4. Join code (student flow)

- **Teacher:** Each **course** (class) has a **join code** (short, e.g. 5 characters). Teacher gets it from the backend when viewing a course (e.g. GET course by id or list teacher courses). Teacher shows this code in class; students enter it when completing profile.
- **Student (first time):** In “Complete your profile”, after choosing role **Student**, ask for the **class join code**. Send it as `joinCode` in `POST /auth/complete-profile` with `userType: "students"`. Backend creates the student and enrolls them in that class.
- **Student (already has account):** To join another class later, use `POST /api/v1/enrollments/join` with `student_id` and body `{ "join_code": "AB12X" }` (see enrollments API). No change to auth flow.

---

## 5. Recommended UI flow

1. **Login screen:** “Sign in with Google” (link to `/api/v1/auth/oauth/redirect` or fetch `/api/v1/auth/oauth/url` and redirect to `url`) and optionally “Sign in with email” (form → `POST /auth/login`).
2. After login, store tokens and call **GET /auth/me**.
3. **If `profile` is null and `user_type` is not `"admin"`:**
   - Screen: “Complete your profile.”
   - Step 1: “I am a…” → **Student** | **Teacher**.
   - Step 2:
     - If Student: “Enter your class join code” (from teacher) → input `joinCode`; or “Or select school” → pick `schoolId` (if your UI supports it).
     - If Teacher: “Select your school” → pick `schoolId`.
   - Submit → **POST /auth/complete-profile** with `userType` and `joinCode` or `schoolId`.
4. **If `profile` is not null (or `user_type === "admin"`):** Go to role-specific app (student dashboard, teacher dashboard, admin).
5. **Optional:** “Edit profile” or “Personal info” screen → **PATCH /auth/me** with `firstName`, `lastName`.

---

## 6. Admin

- **Not** selectable in the UI. Admins are configured on the backend (e.g. list of emails or Cognito ids in env).
- When such a user calls GET /me, backend returns `user_type: "admin"` and `profile: null` (unless you add an admin profile later).
- Frontend should show admin-only sections or routes when `user_type === "admin"`. Backend may enforce admin with a separate dependency on admin-only endpoints.

---

## 7. CORS and callback URL

- Backend should allow the frontend origin in CORS when calling `/api/v1/auth/*` and other APIs.
- For Google sign-in, Cognito redirects to the **backend** callback URL (the value of `OAUTH_CALLBACK_URI`). After the backend returns (HTML or JSON), the frontend can either:
  - Use a dedicated “post-login” page that reads tokens from the response (e.g. if backend returns HTML with tokens in a script or redirects to frontend with tokens in fragment/query), or
  - Have the backend return JSON and the callback URL point to the frontend with `?code=...` and have the frontend send the code to the backend to exchange for tokens (if you add such an endpoint). Current contract: callback is the backend URL; backend exchanges code and returns tokens (HTML or JSON).

---

## 8. Summary table

| Action | Endpoint | When |
|--------|----------|------|
| Sign in with Google | GET `/auth/oauth/url` or GET `/auth/oauth/redirect` | Login page |
| Handle OAuth callback | GET `/auth/callback?code=...` | After Cognito redirect; backend returns tokens |
| Sign in with password | POST `/auth/login` | Login form submit |
| Get current user / role / profile | GET `/auth/me` | After login; before app shell |
| Complete profile (student with join code) | POST `/auth/complete-profile` body `{ userType: "students", joinCode: "AB12X" }` | First time, no profile |
| Complete profile (student with school) | POST `/auth/complete-profile` body `{ userType: "students", schoolId: "<uuid>" }` | First time, no profile |
| Complete profile (teacher) | POST `/auth/complete-profile` body `{ userType: "teachers", schoolId: "<uuid>" }` | First time, no profile |
| Update personal info | PATCH `/auth/me` body `{ firstName?, lastName? }` | Profile edit screen |
| Refresh token | POST `/auth/refresh` | When access token expires |
| Logout | POST `/auth/logout` | Logout action |

This contract is the source of truth for the frontend; backend implements it as in `app/routers/auth.py` and `app/schemas/auth.py`.

---

## 9. ADR – Decisions (summary)

This section summarizes key decisions that affect the auth/profile contract.  
The **canonical backend ADRs** live under `docs/adrs/`, for example:

- `docs/adrs/0002-auth-callback-redirect.md` – OAuth callback behavior and redirect strategy.

| Decision | Rationale |
|----------|-----------|
| **One “complete profile” step** | After login we don’t know school or role from OAuth; one step collects role (student/teacher) and context (join code or school). |
| **Student: join code OR schoolId** | Joining via class code is the primary path (teacher shows code in class); schoolId-only supports admin-created or manual assignment. |
| **Admin not in UI** | Admin is assigned via backend config (e.g. env list); frontend only reacts to `user_type: "admin"` from GET /me. |
| **Profile linked by cognito_user_id** | Students and teachers store `cognito_user_id` (Cognito sub); GET /me and complete-profile use it so one Cognito identity maps to at most one student or one teacher. |
| **PATCH /me for personal info** | Keeps complete-profile minimal (role + context); name/email from token. Optional personal info (e.g. first/last name) editable later via PATCH /me. |
| **OAuth callback on backend** | Cognito redirect_uri is the backend; backend exchanges code for tokens and returns HTML or JSON or a redirect so the same callback works for browser and API clients. |
