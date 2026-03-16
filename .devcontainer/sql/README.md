# SQL Scripts for Archimedes Education Platform

This directory contains all SQL scripts for setting up and managing the PostgreSQL database.

## 📁 Files

### **01_create_schema.sql**
- Creates all database tables, indexes, and constraints
- Includes UUID extension setup
- Run this first to create the complete database structure

### **02_sample_data.sql** 
- Inserts sample schools, teachers, students, courses, and enrollments
- Useful for development and testing
- Run after `01_create_schema.sql`

### **03_queries.sql**
- Common query patterns and examples
- Includes authentication, dashboard, and analytics queries
- Reference for application development

**Activities and assignments:** Schema creates `topics`, `subtopics`, `activities`, and `assignments` tables. The backend seeds in order: topics and subtopics from `docs/topics.csv`, then activities from `docs/miniquiz-activities.csv` (when tables are empty, on startup or via `python scripts/seed_activities.py`).

### **05_migrate_classes_to_courses.sql**
- One-off migration for existing databases that still have table `classes` and column `class_id`. Renames table to `courses`, column to `course_id`, and `class_name` to `course_name`. New installs use `01_create_schema.sql` only.

### **05b_fix_assignments_fk_to_courses.sql**
- Use when you see: *"assignments violates foreign key constraint ... is not present in table 'classes'"*. This happens if the app created table `courses` (e.g. via SQLAlchemy `create_all`) before migration 05 ran, so `assignments.course_id` still references `classes`. This script repoints the FK to `courses`. Run against the same DB as the app.

### **06_migrate_activities_to_taxonomy.sql**
- One-off migration for existing databases that have `activities` with `topic`/`subtopic` columns. Creates `topics` and `subtopics` tables, backfills from activities, adds `activities.subtopic_id`, then drops `topic`/`subtopic`. New installs use `01_create_schema.sql` only.

## 🚀 Quick Setup

```bash
# 1. Create database
createdb archimedes_education

# 2. Run schema creation
psql archimedes_education -f sql/01_create_schema.sql

# 3. Insert sample data
psql archimedes_education -f sql/02_sample_data.sql

# 4. Test with example queries
psql archimedes_education -f sql/03_queries.sql
```

## 🔧 Development Workflow

1. **Schema Changes:** Update `01_create_schema.sql`
2. **Test Data:** Modify `02_sample_data.sql` 
3. **New Queries:** Add to `03_queries.sql`
4. **Migrations:** Use Alembic for production schema changes

## 📋 Connection Details

- **Database:** `archimedes_education`  
- **Default Port:** 5432
- **Required Extensions:** `pgcrypto` (for UUID generation)

## 🎯 Join Code Examples

After running sample data, try these join codes:
- `ALG7M` - Algebra I class
- `GEO8A` - Geometry class  
- `ENG7B` - English Literature
- `CHEM9` - Advanced Chemistry

## 📈 Sample Queries

```sql
-- Find a course by join code
SELECT * FROM courses WHERE join_code = 'ALG7M';

-- Get all students in a course
SELECT s.full_name
FROM students s
JOIN enrollments e ON s.id = e.student_id
JOIN courses c ON e.course_id = c.id
WHERE c.join_code = 'ALG7M' AND e.enrollment_status = 'active';
```

See `03_queries.sql` for more examples!