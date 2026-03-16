-- Add assignment_completions table for tracking which students completed which assignments.
-- Run on existing databases that don't have this table yet.

CREATE TABLE IF NOT EXISTS assignment_completions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    assignment_id UUID NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
    completed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    score NUMERIC(5,2),
    UNIQUE(student_id, assignment_id)
);

CREATE INDEX IF NOT EXISTS idx_assignment_completions_assignment ON assignment_completions(assignment_id);
CREATE INDEX IF NOT EXISTS idx_assignment_completions_student ON assignment_completions(student_id);
