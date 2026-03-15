-- One-off migration: add topics and subtopics tables, move activities to use subtopic_id.
-- Run only on an existing database that has activities with topic/subtopic columns.
-- New installs use 01_create_schema.sql which already has topics, subtopics, and activities.subtopic_id.

-- 1. Create topics table
CREATE TABLE IF NOT EXISTS topics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) UNIQUE NOT NULL,
    display_order INTEGER
);
CREATE INDEX IF NOT EXISTS idx_topics_name ON topics(name);

-- 2. Create subtopics table
CREATE TABLE IF NOT EXISTS subtopics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic_id UUID NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    display_order INTEGER,
    UNIQUE(topic_id, name)
);
CREATE INDEX IF NOT EXISTS idx_subtopics_topic ON subtopics(topic_id);
CREATE INDEX IF NOT EXISTS idx_subtopics_name ON subtopics(name);

-- 3. Populate topics from distinct activity.topic
INSERT INTO topics (id, name)
SELECT gen_random_uuid(), topic
FROM (SELECT DISTINCT topic FROM activities) AS t
ON CONFLICT (name) DO NOTHING;

-- 4. Populate subtopics from distinct (topic, subtopic) in activities
INSERT INTO subtopics (id, topic_id, name)
SELECT gen_random_uuid(), top.id, a.subtopic
FROM (SELECT DISTINCT topic, subtopic FROM activities) AS a
JOIN topics top ON top.name = a.topic
ON CONFLICT (topic_id, name) DO NOTHING;

-- 5. Add subtopic_id to activities (nullable first)
ALTER TABLE activities ADD COLUMN IF NOT EXISTS subtopic_id UUID REFERENCES subtopics(id) ON DELETE RESTRICT;

-- 6. Backfill subtopic_id
UPDATE activities a
SET subtopic_id = s.id
FROM subtopics s
JOIN topics t ON t.id = s.topic_id
WHERE t.name = a.topic AND s.name = a.subtopic;

-- 7. Drop old topic/subtopic columns (after backfill)
ALTER TABLE activities DROP COLUMN IF EXISTS topic;
ALTER TABLE activities DROP COLUMN IF EXISTS subtopic;

-- 8. Make subtopic_id NOT NULL (if any row has NULL, fix data first)
ALTER TABLE activities ALTER COLUMN subtopic_id SET NOT NULL;

-- 9. Drop old indexes and add new one
DROP INDEX IF EXISTS idx_activities_topic;
DROP INDEX IF EXISTS idx_activities_topic_subtopic;
CREATE INDEX IF NOT EXISTS idx_activities_subtopic ON activities(subtopic_id);
