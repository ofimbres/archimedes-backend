-- One-off migration: rename classes -> courses, class_id -> course_id, class_name -> course_name
-- Run this only on an existing database that still has the old schema (table "classes", enrollments.class_id).
-- New installs should use 01_create_schema.sql which already creates "courses" and course_id.

-- 1. Rename table classes to courses
ALTER TABLE classes RENAME TO courses;

-- 2. Rename column class_name to course_name in courses
ALTER TABLE courses RENAME COLUMN class_name TO course_name;

-- 3. Rename indexes on courses (idx_classes_* -> idx_courses_*)
ALTER INDEX idx_classes_school RENAME TO idx_courses_school;
ALTER INDEX idx_classes_teacher RENAME TO idx_courses_teacher;
ALTER INDEX idx_classes_active RENAME TO idx_courses_active;
ALTER INDEX idx_classes_year RENAME TO idx_courses_year;
ALTER INDEX idx_classes_join_code RENAME TO idx_courses_join_code;

-- 4. Enrollments: rename column class_id to course_id
ALTER TABLE enrollments RENAME COLUMN class_id TO course_id;

-- 5. Enrollments: unique constraint on (student_id, course_id) unchanged; column rename is enough.

-- 6. Enrollments: rename index
ALTER INDEX idx_enrollments_class RENAME TO idx_enrollments_course;

-- 7. Enrollments: drop and recreate partial index (it references class_id)
DROP INDEX IF EXISTS idx_enrollments_active;
CREATE INDEX idx_enrollments_active ON enrollments(course_id, enrollment_status)
WHERE enrollment_status = 'active';

-- 8. Assignments FK already references classes(id); PostgreSQL renames the referenced table
--    so the constraint now points to "courses". Constraint name may still reference old table;
--    no need to change unless you want to rename the constraint.
