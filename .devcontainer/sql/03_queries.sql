-- Common query patterns for Archimedes Education Platform
-- Useful for testing and development

-- =============================================================================
-- 1. SCHOOL OPERATIONS
-- =============================================================================

-- Get all schools
SELECT * FROM schools ORDER BY name;

-- Get school by code
SELECT * FROM schools WHERE school_code = 'LAMAR_MIDDLE';

-- =============================================================================
-- 2. USER AUTHENTICATION
-- =============================================================================

-- Find teacher by email
SELECT t.*, s.name as school_name 
FROM teachers t 
JOIN schools s ON t.school_id = s.id 
WHERE t.email = 'guadalupe.trevino@gmail.com';

-- Find student by email  
SELECT st.*, s.name as school_name
FROM students st
JOIN schools s ON st.school_id = s.id
WHERE st.email = 'diego.hinojosa@gmail.com';

-- =============================================================================
-- 3. SCHOOL-BASED QUERIES
-- =============================================================================

-- All teachers in a school
SELECT * FROM teachers 
WHERE school_id = '123e4567-e89b-12d3-a456-426614174000' 
ORDER BY full_name;

-- All students in a school
SELECT * FROM students 
WHERE school_id = '123e4567-e89b-12d3-a456-426614174000' 
ORDER BY full_name;

-- All courses in a school
SELECT c.*, t.full_name as teacher_name
FROM courses c
JOIN teachers t ON c.teacher_id = t.id
WHERE c.school_id = '123e4567-e89b-12d3-a456-426614174000'
ORDER BY c.course_name;

-- =============================================================================
-- 4. TEACHER OPERATIONS
-- =============================================================================

-- Get all courses for a teacher
SELECT * FROM courses
WHERE teacher_id = '456e7890-e89b-12d3-a456-426614174001'
ORDER BY course_name;

-- Get teacher's student count across all courses
SELECT 
    t.full_name,
    COUNT(e.student_id) as total_students
FROM teachers t
JOIN courses c ON t.id = c.teacher_id
JOIN enrollments e ON c.id = e.course_id
WHERE t.id = '456e7890-e89b-12d3-a456-426614174001'
AND e.enrollment_status = 'active'
GROUP BY t.id, t.full_name;

-- =============================================================================
-- 5. COURSE & ENROLLMENT OPERATIONS
-- =============================================================================

-- Get all students in a course
SELECT s.*, e.enrolled_at, e.enrollment_status
FROM students s
JOIN enrollments e ON s.id = e.student_id
WHERE e.course_id = 'abc1234d-e89b-12d3-a456-426614174003'
AND e.enrollment_status = 'active'
ORDER BY s.full_name;

-- Get all courses for a student
SELECT c.*, t.full_name as teacher_name, e.enrollment_status
FROM courses c
JOIN teachers t ON c.teacher_id = t.id
JOIN enrollments e ON c.id = e.course_id
WHERE e.student_id = '789e0123-e89b-12d3-a456-426614174002'
ORDER BY c.course_name;

-- =============================================================================
-- 6. JOIN CODE OPERATIONS
-- =============================================================================

-- Find course by join code (for student enrollment)
SELECT c.*, t.full_name as teacher_name, s.name as school_name
FROM courses c
JOIN teachers t ON c.teacher_id = t.id
JOIN schools s ON c.school_id = s.id
WHERE c.join_code = 'ALG7M';

-- Get teacher's join codes (for sharing with students)
SELECT c.course_name, c.join_code, c.subject
FROM courses c
WHERE c.teacher_id = '456e7890-e89b-12d3-a456-426614174001'
AND c.is_active = TRUE
ORDER BY c.course_name;

-- =============================================================================
-- 7. ANALYTICS QUERIES
-- =============================================================================

-- School enrollment statistics
SELECT 
    s.name as school_name,
    COUNT(DISTINCT st.id) as total_students,
    COUNT(DISTINCT t.id) as total_teachers,
    COUNT(DISTINCT c.id) as total_courses,
    COUNT(e.id) as total_enrollments
FROM schools s
LEFT JOIN students st ON s.id = st.school_id AND st.is_active = TRUE
LEFT JOIN teachers t ON s.id = t.school_id AND t.is_active = TRUE  
LEFT JOIN courses c ON s.id = c.school_id AND c.is_active = TRUE
LEFT JOIN enrollments e ON c.id = e.course_id AND e.enrollment_status = 'active'
GROUP BY s.id, s.name
ORDER BY s.name;

-- Most popular subjects
SELECT 
    c.subject,
    COUNT(e.student_id) as enrolled_students
FROM courses c
JOIN enrollments e ON c.id = e.course_id
WHERE e.enrollment_status = 'active'
GROUP BY c.subject
ORDER BY enrolled_students DESC;

-- Students per grade level
SELECT 
    grade_level,
    COUNT(*) as student_count
FROM students 
WHERE is_active = TRUE
GROUP BY grade_level
ORDER BY grade_level;