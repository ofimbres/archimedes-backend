-- Fix assignments FK when "courses" already exists (e.g. app create_all created it).
-- Use this if you get: "assignments_course_id_fkey ... is not present in table 'classes'".
-- Run this when you have table "courses" and assignments still references "classes".

-- Drop the FK that points at the old "classes" table
ALTER TABLE assignments DROP CONSTRAINT IF EXISTS assignments_course_id_fkey;

-- Point assignments.course_id at "courses" instead
ALTER TABLE assignments
  ADD CONSTRAINT assignments_course_id_fkey
  FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE;
