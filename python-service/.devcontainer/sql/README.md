# SQL Scripts for Archimedes Education Platform

This directory contains all SQL scripts for setting up and managing the PostgreSQL database.

## 📁 Files

### **01_create_schema.sql**
- Creates all database tables, indexes, and constraints
- Includes UUID extension setup
- Run this first to create the complete database structure

### **02_sample_data.sql** 
- Inserts sample schools, teachers, students, periods, and enrollments
- Useful for development and testing
- Run after `01_create_schema.sql`

### **03_queries.sql**
- Common query patterns and examples
- Includes authentication, dashboard, and analytics queries
- Reference for application development

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
-- Find a class by join code
SELECT * FROM periods WHERE join_code = 'ALG7M';

-- Get all students in a class
SELECT s.full_name 
FROM students s
JOIN enrollments e ON s.id = e.student_id  
JOIN periods p ON e.period_id = p.id
WHERE p.join_code = 'ALG7M' AND e.status = 'ACTIVE';
```

See `03_queries.sql` for more examples!