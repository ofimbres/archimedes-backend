## Archimedes Backend – Product Requirements Doc (PRD)

This PRD describes the backend service that powers the Archimedes education platform. It focuses on the **problems, users, use cases, and constraints** this backend must support. Implementation details and specific architecture decisions live in ADRs and technical docs under `docs/`.

---

## Overview

Archimedes is a school-focused learning platform for teachers and students. This backend service provides:

- **Multi-tenant school management** (schools, teachers, students, courses).
- **Authentication and identity integration** via AWS Cognito (Google + email/password).
- **Class enrollment** using short join codes.
- **Assignments**: teachers assign activities (e.g. miniquiz) to courses; students see assignments, open hosted miniquiz/HTML via **`activity.content_url`** in a new tab, and record completion (see ADR 0004, ADR 0005).
- **Foundations for analytics and reporting** across schools, courses, and students.

The backend exposes a clean HTTP API (FastAPI) that is consumed by:

- The **React frontend** (teacher and student apps).
- Internal tools or scripts (e.g. admin operations, data migrations).

---

## Goals & non-goals

### Goals

- **Reliable identity and profile management**
  - Map Cognito identities to internal teacher/student records.
  - Support first-time sign-in flows and “complete your profile” UX.

- **Simple classroom model**
  - Represent schools, teachers, courses, students, and enrollments.
  - Allow students to join courses via short join codes.

- **Safe multi-tenancy**
  - Ensure data is clearly scoped by school.
  - Enable future per-school analytics and reporting.

- **Well-documented contracts**
  - Keep clear, stable API and auth contracts for the frontend and infra.

- **Browser-safe API configuration (frontend)**  
  - The SPA must call the API at **`http://localhost:...`**, **`http://127.0.0.1:...`**, or a **public HTTPS** host. Do **not** use **`http://0.0.0.0:...`** as the fetch base URL (browsers treat it as invalid; preflight fails with CORS-like errors). Binding the server to `0.0.0.0` in Docker/uvicorn is fine **server-side** only.

### Non-goals (for this backend)

- **Rich LMS features** (full grading UI, content authoring) – assignment-to-course is in scope (ADR 0004); deeper grading/authoring can be separate or future phases.
- **Deep analytics dashboards** – backend will expose the data, but full analytics productization is out of scope for now.
- **Multi-platform clients** (native mobile apps) – web-first; APIs may support mobile later but are not optimized here yet.

---

## Users & use cases

### Primary users

- **Teachers**
  - Create and manage courses.
  - On account creation, the system auto-creates up to six default courses (configurable via `max_classes`); each has a unique join code. Teachers can rename and use them or create additional courses **up to their plan limit** (see below).
  - **Course limit (free tier):** A teacher may have at most `max_classes` courses (default **6**). The backend rejects `POST /api/v1/courses/` with **403 Forbidden** when the limit is reached.
  - **Frontend (course limit):** When `POST /api/v1/courses/` returns **403**, the response body is `{"detail": "Course limit reached (maximum N courses for your plan)"}` (N = teacher’s `max_classes` or 6). Show a message such as: *“You’ve reached the maximum number of courses for your plan (N). Upgrade to add more.”* Optionally link to an upgrade or pricing page.
  - Share join codes with students.
  - View rosters and basic engagement metrics.
  - **Set assignments**: search the activity catalog by topic/subtopic and assign activities (e.g. miniquiz) to a course; optionally set a due date.

- **Students**
  - Join a course using a join code or school selection.
  - See assignments for their enrolled courses; open each assignment using the API’s **`assignment.activity.content_url`** in a **new tab** (not an in-app iframe by default; see ADR 0005). Complete the miniquiz/HTML, then the app records completion via **`POST /api/v1/assignments/{id}/completions`** (worksheet/session flow may still apply for some flows; `activity_id` aligns with worksheet id where used).
  - Access course content and activities (via frontend).

- **Admins**
  - Configure schools and high-level settings.
  - Seed or manage teacher accounts.

### Key use cases

- Teacher signs in (Google or password) and lands in the teacher dashboard.
- Teacher signs up → receives auto-created default courses (e.g. 6) with join codes → can rename courses and share codes with students.
- Teacher assigns activities to a course: search activities by topic → select activity → create assignment for course (optional due date).
- Student signs in (Google) for the first time, completes profile:
  - Chooses role (student/teacher).
  - Provides join code or school.
- Student is enrolled into the appropriate course via join code.
- Admin can inspect or export school-level data for analysis.

### User journey: student registration with join code

- **Teacher:** Create or view a course in the app → see the auto-generated join code → share it with the class (e.g. on board, handout, or in-app).
- **Student (first time):** Sign in → “Complete your profile” → choose role **Student** → enter the join code the teacher provided → submit → backend creates student and enrolls in that course.
- **Student (already has account):** “Join another course” → enter the join code → submit → backend enrolls in the course.

API details (request bodies, endpoints) are in `auth-and-profile-contract.md` (complete-profile and enrollments/join).

---

## Requirements

### Functional requirements

- **Authentication**
  - Accept OAuth-based sign-in via Cognito (Google).
  - Optionally support username/password sign-in.
  - Expose a stable set of auth endpoints as described in `AUTH_AND_PROFILE_CONTRACT.md`.

- **Profile completion**
  - Track whether a Cognito user has a linked internal profile.
  - Provide a clear “complete profile” API for frontend to create student/teacher records.

- **Course enrollment**
  - Generate short, human-friendly **join codes** for courses.
  - Allow students to join a course using a code.
  - Support listing courses and enrollments for a teacher or student.

- **Assignments and activities**
  - Maintain a **taxonomy** (topics, subtopics tables) and an **activities** catalog (e.g. miniquiz); activities link via subtopic_id. Seed topics and subtopics from `docs/topics.csv`, then activities from `docs/miniquiz-activities.csv` when empty.
  - Let teachers create **assignments** (course + activity + optional due date); only the course owner can create.
  - Expose list of assignments per course: **`GET /api/v1/assignments/courses/{course_id}`** requires **`Authorization: Bearer`**; caller must be an **enrolled active student**, the **course-owning teacher**, or **platform admin**. Each row includes nested **activity** (including **`content_url`** when the miniquiz base URL env is set). For **students**, optional **`my_completed_at`** / **`my_score`** are included (see ADR 0005). **`GET /api/v1/assignments/{assignment_id}/progress`** is **teacher (course owner) or admin** only. Completion: **`POST /api/v1/assignments/{assignment_id}/completions`** with Bearer; **`student_id`** must match the JWT-linked student. **Enrollments:** **`POST /api/v1/enrollments/join`** and **`GET /api/v1/enrollments/student/{student_id}`** require Bearer and matching student (or admin for the GET). **CORS:** explicit origins (`CORS_ORIGINS`, **`FRONTEND_URL`**, and **http + https** for the miniquiz CDN host derived from **`MINIQUIZ_BASE_URL`**); see `docs/auth-and-profile-contract.md`. **JWT:** Cognito **ID** and **access** tokens are both accepted (`aud` vs `client_id` validation).

- **Data access**
  - Provide APIs that are aligned with the conceptual schema in `postgresql-schema.md`.
  - Keep queries efficient for common dashboard-style views.

### Non-functional requirements

- **Performance**
  - Low-latency responses for core flows (auth, profile, course lists, enrollments) under typical school load.

- **Security**
  - Enforce authorization checks based on roles (student/teacher/admin).
  - Validate JWTs from Cognito and protect all non-public endpoints.
  - Avoid leaking data across schools (multi-tenant isolation).

- **Reliability**
  - Safe, transactional operations for enrollment and profile creation.
  - Migration-friendly schema evolution (via Alembic).

---

## Domain model / concepts (high level)

High-level entities (see `postgresql-schema.md` for details):

- **School**
  - Top-level tenant; owns teachers, students, and courses.
- **Teacher**
  - Belongs to a school; can own multiple courses.
- **Student**
  - Belongs to a school; can enroll in multiple courses.
- **Course**
  - Belongs to a school and a teacher; has a **join code**.
- **Enrollment**
  - Links a student to a course; tracks status (active, dropped, completed).
- **Topic**
  - Taxonomy root for the activity catalog (e.g. Algebra, Arithmetic Operations); seeded from `docs/topics.csv`.
- **Subtopic**
  - Belongs to a topic; activities reference a subtopic. Seeded from `docs/topics.csv` (TOPIC, SUBTOPIC rows).
- **Activity**
  - Catalog item (e.g. miniquiz) with activity_id, linked to taxonomy via subtopic_id (topic/subtopic from join); assignable to courses.
- **Assignment**
  - Links a course to an activity (teacher-assigned); optional due date; all enrolled students see it.

Authentication-related concepts (see `auth-and-profile-contract.md`):

- **Cognito user**
  - External identity (Google or password user) that signs in.
- **User profile**
  - Internal representation as either a student or teacher, linked to Cognito.

---

## Role storage and subscription tier (design)

### Where role (Teacher, Student) is stored

- **In the database only.** The backend links a Cognito identity to an internal profile via `cognito_user_id` on the `students` and `teachers` tables. A user’s role is determined by which table has a row for that Cognito user (and by config for admin).
- **Cognito groups are not used** for role. Keeping role in the DB avoids duplicating state in Cognito and keeps it aligned with app data (school, courses, enrollments). If we later need Cognito groups (e.g. for API Gateway or SSO), they would be additive.

### Free trial vs premium

- **Requirement:** The product must distinguish users (or schools) on a **free trial** vs **premium** plan for billing and feature gating.
- **Design (approved for future implementation):**
  - **School-level default:** Each school has a subscription tier (e.g. `free_trial` | `premium`). New schools default to `free_trial`. This is the default for everyone in that school.
  - **Optional user override:** Each teacher and each student can have an optional override. If set, that user’s effective tier is the override; if not set, the effective tier is the school’s tier (e.g. a premium teacher in a free-trial school).
  - **Effective tier rule:** For any user, effective tier = user’s subscription_tier if present, else the school’s subscription_tier.
  - **Storage:** All tier data lives in the database (school and user tables). No Cognito custom attributes or groups for subscription tier.
- **Exposure:** The effective subscription tier for the current user will be exposed to the frontend (e.g. in the profile or `/me` response) so the UI can gate features or show upgrade prompts. No implementation is required until we are ready to ship this.

### Existing courses over the limit

- Teachers who already have more courses than their `max_classes` (e.g. 9 when limit is 6) keep all existing courses; the limit is enforced only when **creating** a new course.
- **Recommendation:** Do not delete or deactivate those extra courses automatically (avoids breaking enrollments and existing course data). Treat them as grandfathered. If product later wants a one-time cleanup (e.g. deactivate or archive courses beyond the limit), that can be a separate migration or admin action.

---

## Dependencies & integrations

- **AWS Cognito**
  - Manages user identities and tokens.
  - Backend expects env vars and stack outputs described in `infra-backend-contract.md`.

- **Archimedes infra stack**
  - Provides database, Cognito user pool, and networking as documented in `infra-backend-contract.md`, `google-auth-setup.md`, `email-setup.md`, and `signed-urls-setup.md`.

- **Frontend app**
  - Consumes the auth and profile API as defined in `auth-and-profile-contract.md`.
  - Implements the custom auth UI flow described in `custom-auth-ui-plan.md`.

---

## Implementation status (backend vs frontend)

This section records what this backend provides and what the frontend (other repo) must implement for the documented user journeys.

**Backend (implemented):**

- **Teacher courses and join codes:** `GET /api/v1/courses/teacher/{teacher_id}` returns a list of courses; `GET /api/v1/courses/{course_id}` returns a single course. Both responses include `join_code`. Teachers can use these to display or share codes once the frontend calls them.
- **Student join-code flow:** `POST /api/v1/auth/complete-profile` (with `joinCode`) and `POST /api/v1/enrollments/join` (with `join_code`) are implemented. Students can register or join a class using the code the teacher provides.
- **Teacher default courses:** On teacher account creation, the backend auto-creates up to six default courses with unique join codes (see ADR 0003).
- **Assignments and activities:** `GET /api/v1/activities` (filter by topic, subtopic), `GET /api/v1/activities/topics`, `GET /api/v1/activities/{activity_id}` (lookup by exercise id), `POST /api/v1/assignments`, `GET /api/v1/assignments/courses/{course_id}` (**Bearer required**; student enrolled, teacher owner, or admin), `GET /api/v1/assignments/{assignment_id}/progress` (teacher owner or admin), `POST /api/v1/assignments/{assignment_id}/completions` (student Bearer + body `student_id` match). Taxonomy and activities seeded from `docs/topics.csv` and `docs/miniquiz-activities.csv` on first run (app startup when tables are empty) or via `python scripts/seed_activities.py` (see ADR 0004). Student launch, miniquiz `fetch`, and CORS: ADR 0005 and `auth-and-profile-contract.md`.

**Frontend gap (teacher flow to display join code):**

- **Teacher Home / “My classes”:** The contract (auth-and-profile-contract.md) says: “Teacher gets [the join code] from the backend when viewing a course (e.g. GET course by id or list teacher courses). Teacher shows this code in class.” Today, the teacher UI does not yet call these course APIs or render courses with their join codes. To complete the flow:
  - From the teacher area (e.g. Teacher Home or “My classes”), call `GET /api/v1/courses/teacher/{teacher_id}` (using the teacher id from the current user’s profile).
  - Render each course (e.g. name, subject) and its **join_code** so the teacher can show or copy it in class.

**Frontend (assignments):** Teacher UI can call `GET /api/v1/activities?topic=...` to search, then `POST /api/v1/assignments` with `teacher_id` from `GET /auth/me` profile. Student UI calls `GET /api/v1/assignments/courses/{course_id}` **with Bearer** (ID or access token). Build miniquiz launch URLs with query **`assignment_id`**, **`archimedes_api_base`**, **`student_id`**, optional **`activity_id`**, and put **`#access_token=...`** in the hash for `m4u_extended.js` to **`POST .../completions`** (see `src/pages/student/Assignments.tsx`). Use **`http://localhost:8001`** (or your public API URL), not **`0.0.0.0`**, as the API base in the browser. Details: `docs/auth-and-profile-contract.md`, ADR 0005.

---

## Open questions (candidates for ADRs)

- How far do we push analytics into this backend vs. a separate reporting service?
- What is the long-term strategy for admin features (separate admin app vs. roles within the main frontend)?
- How should we version public APIs if we expose them to third parties?
- How do we handle soft deletion vs. hard deletion for core entities over time?

When these questions are answered, capture the decisions in `docs/adrs/NNNN-*.md` ADR files.

