-- Sample data for Archimedes Education Platform
-- Run this after 01_create_schema.sql

-- =============================================================================
-- SAMPLE SCHOOLS
-- =============================================================================
INSERT INTO schools (id, school_code, name, address, principal_name, contact_email, phone_number) VALUES 
(
    '123e4567-e89b-12d3-a456-426614174000',
    'LAMAR_MIDDLE',
    'LAMAR MIDDLE SCHOOL',
    '1818 N ARKANSAS AVE, LAREDO, TX, 78043-3097',
    'MR EDUARDO LOPEZ',
    'elopez2@laredoisd.org',
    '(956) 273-6200'
),
(
    '223e4567-e89b-12d3-a456-426614174001',
    'RIVERSIDE_HIGH',
    'RIVERSIDE HIGH SCHOOL',
    '1234 Main St, Austin, TX, 78701',
    'MS MARIA GARCIA',
    'mgarcia@austinisd.org',
    '(512) 555-0123'
);

-- =============================================================================
-- SAMPLE TEACHERS
-- =============================================================================
INSERT INTO teachers (id, school_id, first_name, last_name, email, username, max_classes) VALUES 
(
    '456e7890-e89b-12d3-a456-426614174001',
    '123e4567-e89b-12d3-a456-426614174000',
    'Guadalupe',
    'Trevino',
    'guadalupe.trevino@gmail.com',
    'guadalupe.trevino',
    6
),
(
    '556e7890-e89b-12d3-a456-426614174002',
    '123e4567-e89b-12d3-a456-426614174000',
    'Robert',
    'Johnson',
    'robert.johnson@gmail.com',
    'robert.johnson',
    6
),
(
    '656e7890-e89b-12d3-a456-426614174003',
    '223e4567-e89b-12d3-a456-426614174001',
    'Sarah',
    'Williams',
    'sarah.williams@gmail.com',
    'sarah.williams',
    8
);

-- =============================================================================
-- SAMPLE STUDENTS
-- =============================================================================
INSERT INTO students (id, school_id, first_name, last_name, email, username) VALUES 
(
    '789e0123-e89b-12d3-a456-426614174002',
    '123e4567-e89b-12d3-a456-426614174000',
    'Diego',
    'Hinojosa',
    'diego.hinojosa@gmail.com',
    'diego.hinojosa'
),
(
    '889e0123-e89b-12d3-a456-426614174003',
    '123e4567-e89b-12d3-a456-426614174000',
    2,
    'Maria',
    'Rodriguez',
    'maria.rodriguez@gmail.com',
    'maria.rodriguez',
    7
),
(
    '989e0123-e89b-12d3-a456-426614174004',
    '123e4567-e89b-12d3-a456-426614174000',
    3,
    'Carlos',
    'Martinez',
    'carlos.martinez@gmail.com',
    'carlos.martinez',
    8
),
(
    'a89e0123-e89b-12d3-a456-426614174005',
    '223e4567-e89b-12d3-a456-426614174001',
    1,
    'Emily',
    'Davis',
    'emily.davis@gmail.com',
    'emily.davis',
    9
);

-- =============================================================================
-- SAMPLE COURSES
-- =============================================================================
INSERT INTO courses (id, school_id, teacher_id, course_name, subject, join_code, academic_year, semester) VALUES
(
    'abc1234d-e89b-12d3-a456-426614174003',
    '123e4567-e89b-12d3-a456-426614174000',
    '456e7890-e89b-12d3-a456-426614174001',
    'Algebra I',
    'Mathematics',
    'ALG7M',
    '2024-25',
    'Full Year'
),
(
    'bbc1234d-e89b-12d3-a456-426614174004',
    '123e4567-e89b-12d3-a456-426614174000',
    '456e7890-e89b-12d3-a456-426614174001',
    'Geometry',
    'Mathematics',
    'GEO8A',
    '2024-25',
    'Full Year'
),
(
    'cbc1234d-e89b-12d3-a456-426614174005',
    '123e4567-e89b-12d3-a456-426614174000',
    '556e7890-e89b-12d3-a456-426614174002',
    3,
    'English Literature',
    'English',
    'ENG7B',
    '2024-25',
    'Full Year'
),
(
    'dbc1234d-e89b-12d3-a456-426614174006',
    '223e4567-e89b-12d3-a456-426614174001',
    '656e7890-e89b-12d3-a456-426614174003',
    1,
    'Advanced Chemistry',
    'Science',
    'CHEM9',
    '2024-25',
    'Full Year'
);

-- =============================================================================
-- SAMPLE ENROLLMENTS
-- =============================================================================
INSERT INTO enrollments (id, student_id, course_id, enrolled_at, enrollment_status) VALUES
(
    'def5678e-e89b-12d3-a456-426614174004',
    '789e0123-e89b-12d3-a456-426614174002',
    'abc1234d-e89b-12d3-a456-426614174003',
    '2024-08-25',
    'ACTIVE'
),
(
    'eef5678e-e89b-12d3-a456-426614174005',
    '889e0123-e89b-12d3-a456-426614174003',
    'cbc1234d-e89b-12d3-a456-426614174005',
    '2024-08-25',
    'ACTIVE'
),
(
    'fef5678e-e89b-12d3-a456-426614174006',
    '989e0123-e89b-12d3-a456-426614174004',
    'abc1234d-e89b-12d3-a456-426614174003',
    '2024-08-25',
    'ACTIVE'
),
(
    'gef5678e-e89b-12d3-a456-426614174007',
    '989e0123-e89b-12d3-a456-426614174004',
    'bbc1234d-e89b-12d3-a456-426614174004',
    '2024-08-25',
    'ACTIVE'
),
(
    'hef5678e-e89b-12d3-a456-426614174008',
    'a89e0123-e89b-12d3-a456-426614174005',
    'dbc1234d-e89b-12d3-a456-426614174006',
    '2024-08-25',
    'ACTIVE'
);