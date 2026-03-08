## ADR 0003 – Auto-create default courses for new teachers

- **Status**: Accepted  
- **Date**: 2026-03-02  
- **Owner**: backend-team  

---

## Context

New teachers need courses and join codes before they can invite students. Two options: (a) auto-create a set of default courses on account creation, or (b) require teachers to create each course manually.

---

## Decision

Auto-create up to **max_classes** (default 6) default courses when a teacher is created. Each course gets a unique join code. Names are “Course 1” … “Course N”; teachers can rename via the API. No extra UI step before they can share a code.

---

## Consequences

### Positive

- Teachers have join codes immediately and can share them with students without creating courses first.
- Consistent onboarding: every new teacher starts with the same structure.

### Negative / risks

- Possible clutter if a teacher needs fewer than 6 courses (they can ignore or rename).
- Default names are generic until the teacher renames them.

### Follow-ups

- None required; behavior is implemented in `TeacherService` and `CourseService`.

---

## Alternatives considered

- **Manual-only creation** – Teachers create each course themselves. More steps; no join codes until they create at least one course. Rejected in favor of faster onboarding.
- **Create courses on first login** – Would add branching (e.g. “set up your classes” wizard). Current choice keeps creation at account-creation time so the teacher always has courses and codes.

---

## References

- PRD: `docs/prd.md` (Users & use cases).
- Implementation: `app/services/teacher_service.py` (`_create_default_courses`), `app/services/course_service.py` (`create_default_course`).
