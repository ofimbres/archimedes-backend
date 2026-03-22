# ADR 0005 – Student assignment launch (`content_url`, new tab)

- **Status**: Accepted
- **Date**: 2026-03
- **Owner**: platform

---

## Context

Students open assigned miniquiz/HTML content hosted on S3 or a CDN. The API already exposes a full **`activity.content_url`** on assignment list/detail responses (see `ActivityResponse` in the backend). Embedding that URL in an in-app iframe is fragile (CSP, iPad Safari, third-party cookie quirks). Product wants a predictable launch UX without introducing signed URLs or extra download endpoints until explicitly required.

---

## Decision

1. **Reuse the API URL**  
   The student UI must use **`assignment.activity.content_url`** from `GET /api/v1/assignments/courses/{course_id}` (and the same field on single-assignment responses). Do not construct a different URL on the client unless product adds a dedicated signed-URL or proxy API later.

2. **New-tab launch**  
   Open the assignment in a **new browsing context** via **`<a href="…" target="_blank" rel="noopener noreferrer">`**. **Do not** load `content_url` in an iframe by default.

3. **Copy and empty URL**  
   - If the student has **not** completed the assignment (pending or past due): primary action label **“Open assignment”**.  
   - If **completed**: **“Review assignment”** (same `content_url`).  
   - If `content_url` is missing or blank: show **“Link unavailable”**; do not throw.  
   Show short helper text that assignments open in a new tab and the Archimedes tab should stay open for return navigation.

4. **Per-student completion on the list (one round-trip)**  
   **`GET /api/v1/assignments/courses/{course_id}`** always requires **`Authorization: Bearer`**. When the user is linked to a **student** row (`students.cognito_user_id` = JWT `sub`) **and** enrolled in the course, each assignment includes **`my_completed_at`** and **`my_score`** (or null if not completed) so the UI can choose “Open” vs “Review” without N+1 completion fetches. **Teachers** and **admins** calling the same endpoint get the list with **`my_*`** null on every row (they are not the enrolled student).

5. **No new signed-URL API**  
   Deferred until product requires private objects or time-limited links; see integration notes in `docs/auth-and-profile-contract.md` (activities & assignments).

6. **Submit → progress from the miniquiz tab**  
   Launch URL query params: **`assignment_id`**, **`archimedes_api_base`**, **`student_id`**, optional **`activity_id`**. Pass the Cognito token in the URL **hash** (`#access_token=...`) or **`window.M4UConfig.archimedes.accessToken`**. **`m4u_extended.js`** **`fetch`**es **`POST {archimedes_api_base}/api/v1/assignments/{assignment_id}/completions`** with Bearer and JSON **`{ student_id, score? }`**. **CORS:** the API merges **`CORS_ORIGINS`**, **`FRONTEND_URL`**, and **both http and https** for the host parsed from **`MINIQUIZ_BASE_URL`** (so the CDN origin is allowed without duplicating every scheme in env). **`student_id`** and JWT **`sub`** must match the linked student on **`POST /completions`**. The SPA must use **`localhost` / `127.0.0.1` / a public API host** as **`archimedes_api_base`**, not **`0.0.0.0`** (browsers reject it).

---

## Consequences

### Positive

- Simple, CSP-friendly student flow aligned with how static miniquiz HTML is hosted today.  
- One list call can drive both link labels and URLs for enrolled students.

### Negative / risks

- Students must manage two tabs; acceptable per product.  
- `content_url` depends on backend env (`S3_MINI_QUIZ_BASE_URL` / `MINIQUIZ_BASE_URL`); if unset, clients correctly show “Link unavailable”.  
- **Public miniquiz → `fetch` to `localhost` / `0.0.0.0`:** Browsers block this (Private Network Access). `archimedes_api_base` must be a **reachable public HTTPS API** in real use, not `http://0.0.0.0:8001`.

---

## References

- PRD: `docs/prd.md` (Assignments, Implementation status, student frontend notes).  
- API contract: `docs/auth-and-profile-contract.md` (section 6, assignments).  
- Reference UI: `src/pages/student/Assignments.tsx` (`buildAssignmentLaunchUrl`, `withAccessTokenHash`, miniquiz `fetch` completion).  
- Miniquiz hook: `deploy/mini-quizzes/m4u_extended.js` (sync to static hosting / `archimedes-infrastructure` as needed).  
- Backend: `app/routers/assignments.py` (list + **`POST` completions with Bearer + student match**), `app/schemas/assignment.py` (`AssignmentResponse.my_*`), `app/services/assignment_service.py` (`list_by_course`, `record_completion`).  
- Related: ADR 0004 (`docs/adrs/0004-activities-and-assignments.md`).
