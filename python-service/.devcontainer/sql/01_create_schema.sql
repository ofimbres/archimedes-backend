-- PostgreSQL Database Schema for Archimedes Education Platform
-- Run this file to create the complete database structure
 -- Enable UUID generation

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- 1. SCHOOLS TABLE
-- =============================================================================

CREATE TABLE schools (id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                  school_code VARCHAR(50) UNIQUE NOT NULL,
                                                                                 name VARCHAR(255) NOT NULL,
                                                                                                   address TEXT, principal_name VARCHAR(255),
                                                                                                                                contact_email VARCHAR(255),
                                                                                                                                              phone_number VARCHAR(20),
                                                                                                                                                           created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                                                                                                                                                                                                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW());

-- Indexes for schools

CREATE UNIQUE INDEX idx_schools_code ON schools(school_code);


CREATE INDEX idx_schools_name ON schools(name);

-- =============================================================================
-- 2. TEACHERS TABLE
-- =============================================================================

CREATE TABLE teachers
    (id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 school_id UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
                                                                                          first_name VARCHAR(50) NOT NULL,
                                                                                                                 last_name VARCHAR(50) NOT NULL,
                                                                                                                                       email VARCHAR(100) UNIQUE NOT NULL,
                                                                                                                                                                 username VARCHAR(50) UNIQUE NOT NULL,
                                                                                                                                                                                             max_classes INTEGER DEFAULT 6,
                                                                                                                                                                                                                         is_active BOOLEAN DEFAULT TRUE,
                                                                                                                                                                                                                                                   created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                                                                                                                                                                                                                                                                                               updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW());

-- Computed column for full name

ALTER TABLE teachers ADD COLUMN full_name VARCHAR(255) GENERATED ALWAYS AS (first_name || ' ' || last_name) STORED;

-- Indexes for teachers

CREATE INDEX idx_teachers_school ON teachers(school_id);


CREATE UNIQUE INDEX idx_teachers_email ON teachers(email);


CREATE INDEX idx_teachers_active ON teachers(school_id, is_active);

-- =============================================================================
-- 3. STUDENTS TABLE
-- =============================================================================

CREATE TABLE students
    (id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 school_id UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
                                                                                          first_name VARCHAR(50) NOT NULL,
                                                                                                                 last_name VARCHAR(50) NOT NULL,
                                                                                                                                       email VARCHAR(100) UNIQUE NOT NULL,
                                                                                                                                                                 username VARCHAR(50) UNIQUE NOT NULL,
                                                                                                                                                                                             is_active BOOLEAN DEFAULT TRUE,
                                                                                                                                                                                                                       created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                                                                                                                                                                                                                                                                   updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW());

-- Computed column for full name

ALTER TABLE students ADD COLUMN full_name VARCHAR(255) GENERATED ALWAYS AS (first_name || ' ' || last_name) STORED;

-- Indexes for students

CREATE INDEX idx_students_school ON students(school_id);


CREATE UNIQUE INDEX idx_students_email ON students(email);


CREATE INDEX idx_students_active ON students(school_id, is_active);

-- =============================================================================
-- 4. CLASSES TABLE
-- =============================================================================

CREATE TABLE classes
    (id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 school_id UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
                                                                                          teacher_id UUID NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
                                                                                                                                                     class_name VARCHAR(100) NOT NULL,
                                                                                                                                                                             subject VARCHAR(50) NOT NULL,
                                                                                                                                                                                                 join_code VARCHAR(10) UNIQUE NOT NULL, -- Easy code for students to join
 academic_year VARCHAR(10) NOT NULL DEFAULT '2024-25',
                                            semester VARCHAR(20) NOT NULL DEFAULT 'Fall',
                                                                                  is_active BOOLEAN DEFAULT TRUE,
                                                                                                            created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                                                                                                                                                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW());

-- Add constraint to ensure class school matches teacher school

ALTER TABLE classes ADD CONSTRAINT check_teacher_school_match CHECK (school_id =
                                                                         (SELECT school_id
                                                                          FROM teachers
                                                                          WHERE id = teacher_id));

-- Indexes for classes

CREATE INDEX idx_classes_school ON classes(school_id);


CREATE INDEX idx_classes_teacher ON classes(teacher_id);


CREATE INDEX idx_classes_active ON classes(school_id, is_active);


CREATE INDEX idx_classes_year ON classes(school_id, academic_year);


CREATE UNIQUE INDEX idx_classes_join_code ON classes(join_code);

-- =============================================================================
-- 5. ENROLLMENTS TABLE (Many-to-Many: Students ↔ Classes)
-- =============================================================================

CREATE TABLE enrollments
    (id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
                                                                                            class_id UUID NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
                                                                                                                                                    enrolled_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                                                                                                                                                                                                 enrollment_status VARCHAR(20) DEFAULT 'active',
                                                                                                                                                                                                                                       is_active BOOLEAN DEFAULT TRUE,
                                                                                                                                                                                                                                                                 created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                                                                                                                                                                                                                                                                                                             updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(), -- Prevent duplicate enrollments
 UNIQUE(student_id, class_id));

-- Indexes for enrollments

CREATE INDEX idx_enrollments_student ON enrollments(student_id);


CREATE INDEX idx_enrollments_class ON enrollments(class_id);


CREATE INDEX idx_enrollments_status ON enrollments(enrollment_status);


CREATE INDEX idx_enrollments_active ON enrollments(class_id, enrollment_status)
WHERE enrollment_status = 'active';