# ADR 0004 – Activities catalog and assignments (teacher assigns to course)

- **Status**: Accepted
- **Date**: 2026-03
- **Owner**: backend-team

---

## Context

Teachers need a way to set work for their courses (e.g. assign a miniquiz or exercise). Students should see what is assigned per course and complete it using the existing worksheet/miniquiz flow. The platform has a catalog of activities (miniquiz) described in `docs/miniquiz-activities.csv` (topic, subtopic, activity_id, description). We need a clear model for “assignable catalog” and “assignment to a course.”

---

## Decision

1. **Taxonomy and activities (catalog)**  
   - **Topics** and **subtopics** tables form a normalized taxonomy; seeded from `docs/topics.csv`. Activities link via `subtopic_id` (FK to subtopics).  
   - **Activities table:** one row per assignable item: `activity_id` (PK), `subtopic_id`, `description`, `activity_type` (e.g. `miniquiz`). Topic/subtopic names come from the join.  
   - Seeded in order: topics → subtopics → activities (from `docs/miniquiz-activities.csv`, resolving TOPIC/SUBTOPIC to subtopic_id). Backend startup when tables are empty, or run `python scripts/seed_activities.py`.  
   - Search via `GET /api/v1/activities?topic=...&subtopic=...&activity_type=...`, `GET /api/v1/activities/topics` for filter UIs, and `GET /api/v1/activities/{activity_id}` for lookup by exercise id.

2. **Assignments table**  
   - Teacher assigns an activity to a **course** (not per-student by default): `course_id`, `activity_id`, `assigned_by` (teacher_id), optional `due_date`, optional `title_override`.  
   - Unique (course_id, activity_id). Only the course owner can create an assignment.  
   - List by course: `GET /api/v1/assignments/courses/{course_id}` (teachers and enrolled students).  
   - Create: `POST /api/v1/assignments` with body `course_id`, `activity_id`, `teacher_id` (from `GET /auth/me`), optional `due_date`, `title_override`.

3. **Completion**  
   - No new completion model: students complete work via existing worksheet sessions; `worksheet_id` = `activity_id`. Assignments only attach an activity to a course (and optional due date).

---

## Consequences

### Positive

- Teachers can search activities by topic/subtopic and assign to a course in one place.
- Single source of truth for catalog (DB) with CSV as initial seed.
- Reuses existing worksheet/miniquiz flow; no duplicate completion tracking.
- Clear authorization: only course owner creates assignments.

### Negative / risks

- Per-student assignment or per-student due dates are out of scope; can be added later (e.g. assignment_recipients table).
- Catalog is seeded once when empty; updating the catalog from CSV later would need a separate admin or migration path.

### Follow-ups

- None required. Optional: admin endpoint to re-seed or upsert activities from CSV; per-student assignment variants if product needs them.

---

## Alternatives considered

- **No activities table, search only in CSV/cache** – Rejected so that search and reporting are DB-backed and consistent.
- **Assignment to student instead of course** – Chosen to keep the default “whole class” and add per-student later if needed.
- **Separate “exercises” table from “activities”** – Rejected; one catalog table with `activity_type` (miniquiz, exercise, …) keeps the model simple.

---

## References

- PRD: `docs/prd.md` (Assignments and activities, Implementation status).
- Schema: `docs/postgresql-schema.md` (Activities table, Assignments table).
- Catalog source: `docs/miniquiz-activities.csv`.
- Implementation: `app/models/activity.py`, `app/models/assignment.py`, `app/services/activity_service.py`, `app/services/assignment_service.py`, `app/routers/activities.py`, `app/routers/assignments.py`.
