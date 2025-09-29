-- Migration: Create user_id_counters table for sequential ID generation
-- This table tracks the next sequence number for each school and user type

CREATE TABLE user_id_counters (
    school_id BIGINT NOT NULL,
    user_type VARCHAR(20) NOT NULL,
    next_sequence INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (school_id, user_type),
    FOREIGN KEY (school_id)  REFERENCES schools(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_user_id_counters_school_id ON user_id_counters(school_id);

-- Insert initial counters for existing schools (if any)
-- This would typically be done during migration
-- INSERT INTO user_id_counters (school_id, user_type, next_sequence) 
-- SELECT id, 'STUDENT', 1 FROM schools;
-- INSERT INTO user_id_counters (school_id, user_type, next_sequence) 
-- SELECT id, 'TEACHER', 1 FROM schools;

COMMENT ON TABLE user_id_counters IS 'Tracks sequential user ID counters per school and user type';
COMMENT ON COLUMN user_id_counters.school_id IS 'Foreign key reference to schools table';
COMMENT ON COLUMN user_id_counters.user_type IS 'Type of user: STUDENT or TEACHER';
COMMENT ON COLUMN user_id_counters.next_sequence IS 'Next sequence number to be assigned';