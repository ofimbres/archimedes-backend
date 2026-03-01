-- Add cognito_user_id to students and teachers for OAuth (Google) identity linking.
-- Run this against your database if you already have existing tables.
-- New deployments using create_all will get the columns from the model.

-- Students
ALTER TABLE students ADD COLUMN IF NOT EXISTS cognito_user_id VARCHAR(255) NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ix_students_cognito_user_id ON students (cognito_user_id) WHERE cognito_user_id IS NOT NULL;

-- Teachers
ALTER TABLE teachers ADD COLUMN IF NOT EXISTS cognito_user_id VARCHAR(255) NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ix_teachers_cognito_user_id ON teachers (cognito_user_id) WHERE cognito_user_id IS NOT NULL;

COMMENT ON COLUMN students.cognito_user_id IS 'Cognito sub (e.g. Google_...) for OAuth users; links to Cognito identity';
COMMENT ON COLUMN teachers.cognito_user_id IS 'Cognito sub (e.g. Google_...) for OAuth users; links to Cognito identity';
