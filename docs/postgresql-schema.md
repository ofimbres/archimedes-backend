# PostgreSQL Database Schema Design - Archimedes Education Platform

## 🎯 **Database Overview**

**Database Name:** `archimedes_education`  
**PostgreSQL Version:** 14+  
**Design Pattern:** Normalized relational design with proper foreign keys and constraints

---

## 📊 **Core Entities & Relationships**

### **Entity Relationship Diagram (Conceptual)**
```
School (1) ←→ (Many) Teacher
School (1) ←→ (Many) Student  
School (1) ←→ (Many) Course
Teacher (1) ←→ (Many) Course
Course (Many) ←→ (Many) Student (via Enrollment)
Teacher (1) ←→ (Many) Assignment
Course (1) ←→ (Many) Assignment
Activity (1) ←→ (Many) Assignment  (catalog: activities table)
```

---

## 🏫 **1. Schools Table**

**Purpose:** Central hub for all school information and multi-tenant organization.

**Key Fields:**
- `id` (UUID) - Primary key for internal relationships
- `school_code` (VARCHAR) - Human-readable identifier (e.g., 'LAMAR_MIDDLE')  
- `name` - Full school name
- `address`, `principal_name`, `contact_email`, `phone_number` - School details
- `created_at`, `updated_at` - Audit timestamps

**Business Rules:**
- Each school has a unique code for user-friendly URLs
- UUIDs for security and distribution
- Cascade deletes remove all related data when school is deleted

---

## 👨‍🏫 **2. Teachers Table**

**Purpose:** Store teacher information scoped to individual schools.

**Key Fields:**
- `id` (UUID) - Global unique identifier
- `school_id` (UUID) - Links to school (foreign key)
- `first_name`, `last_name` - Name components  
- `email` - Global unique for authentication
- `username` - Global unique for login
- `max_classes` - Teaching load limit (default 6)
- `is_active` - Soft delete flag

**Business Rules:**
- Email addresses are globally unique across all schools
- Full name computed in application when needed
- Cascade delete when school is removed

---

## 👨‍🎓 **3. Students Table**

**Purpose:** Store student information scoped to individual schools.

**Key Fields:**
- `id` (UUID) - Global unique identifier  
- `school_id` (UUID) - Links to school (foreign key)
- `first_name`, `last_name` - Name components
- `email` - Global unique for authentication
- `username` - Global unique for login
- `is_active` - Soft delete flag

**Business Rules:**
- Email addresses are globally unique across all schools
- Full name computed in application when needed
- Simplified design focuses on core identity data

---

## 📚 **4. Courses Table**

**Purpose:** Represent courses taught by teachers that students can join.

**Key Fields:**
- `id` (UUID) - Global unique identifier
- `school_id` (UUID) - Links to school (foreign key)
- `teacher_id` (UUID) - Links to teacher (foreign key)
- `course_name` - Displayable course name (e.g., "Algebra I", "English Literature")
- `subject` - Subject category (Mathematics, English, Science, etc.)
- `join_code` - **Easy 5-6 character code for student enrollment** (e.g., 'ALG7M')
- `academic_year` - School year (e.g., '2024-25')
- `semester` - Duration (Fall, Spring, Full Year)
- `is_active` - Soft delete flag

**Join Code System:**
- **Purpose:** Simple way for students to join courses
- **Format:** Random memorable codes (ALG7M, ENG8A, SCI6B, MATH9)
- **Benefits:** Easy to remember, share, and type
- **Uniqueness:** Globally unique across all schools
- **Generation:** Random but can incorporate subject hints

---

## 🎓 **5. Enrollments Table**

**Purpose:** Many-to-many relationship connecting students with courses.

**Key Fields:**
- `id` (UUID) - Global unique identifier
- `student_id` (UUID) - Links to student (foreign key)
- `course_id` (UUID) - Links to course (foreign key)
- `enrolled_at` (TIMESTAMP) - When student joined the course
- `enrollment_status` - Status (active, dropped, completed)
- `is_active` - Soft delete flag

**Business Logic:**
- Students join courses using the course's `join_code`
- One student can be in multiple courses
- One course can have multiple students
- Track enrollment history and status changes

---

## 📂 **6. Topics Table**

**Purpose:** Taxonomy root for the activity catalog (e.g. Algebra, Arithmetic Operations).

**Key Fields:**
- `id` (UUID PK)
- `name` (VARCHAR(200) UNIQUE NOT NULL)
- `display_order` (INTEGER, optional)

**Business Logic:** Seeded from `docs/topics.csv` (distinct TOPIC values) when table is empty. Subtopics reference topics; activities reference subtopics.

---

## 📂 **7. Subtopics Table**

**Purpose:** Child of Topic; each activity belongs to one subtopic.

**Key Fields:**
- `id` (UUID PK)
- `topic_id` (UUID FK → topics.id) ON DELETE CASCADE
- `name` (VARCHAR(200) NOT NULL)
- `display_order` (INTEGER, optional)
- UNIQUE(topic_id, name)

**Business Logic:** Seeded from `docs/topics.csv` (TOPIC, SUBTOPIC rows) after topics. Activities reference subtopic_id.

---

## 📋 **8. Activities Table**

**Purpose:** Catalog of assignable items (miniquiz, exercises). Teachers search by topic/subtopic and assign to courses. Topic and subtopic come from the taxonomy (join via subtopic_id).

**Key Fields:**
- `activity_id` (VARCHAR(20) PK) - e.g. AL01, EX01; matches worksheet IDs
- `subtopic_id` (UUID FK → subtopics.id) ON DELETE RESTRICT
- `description` (VARCHAR(500)) - From catalog
- `activity_type` (VARCHAR(50)) - 'miniquiz' | 'exercise' | ...
- `created_at` (TIMESTAMP)

**Business Logic:**
- Seeded from `docs/miniquiz-activities.csv` when table is empty (after topics and subtopics); (TOPIC, SUBTOPIC) resolved to subtopic_id.
- Search via GET /api/v1/activities?topic=...&subtopic=... (filter by taxonomy names). GET /api/v1/activities/{activity_id} for lookup by exercise id.

---

## 📝 **9. Assignments Table**

**Purpose:** Teacher assigns an activity to a course (all enrolled students see it).

**Key Fields:**
- `id` (UUID PK)
- `course_id` (UUID FK → courses) ON DELETE CASCADE
- `activity_id` (VARCHAR(20) FK → activities.activity_id) ON DELETE CASCADE
- `assigned_by` (UUID FK → teachers) ON DELETE CASCADE
- `due_date` (TIMESTAMP WITH TIME ZONE, optional)
- `title_override` (VARCHAR(200), optional)
- `created_at` (TIMESTAMP WITH TIME ZONE)
- UNIQUE(course_id, activity_id)

**Business Logic:**
- Teacher must own the course to create an assignment
- List by course: GET /api/v1/assignments/courses/{course_id}
- Create: POST /api/v1/assignments (body: course_id, activity_id, teacher_id, optional due_date, title_override)

---

## 🔧 **10. Implementation Strategy**

### **ID Generation Approach:**
- **Application-Level Counters:** Handle ID generation in Python services
- **School-Scoped Numbers:** Teacher/Student/Course numbers unique per school
- **Race Condition Safety:** Unique constraints prevent duplicates
- **Simple & Testable:** No complex database functions needed

### **Join Code Strategy:**
- **Format:** 5-6 character codes (ALG7M, ENG8A, SCI6B)
- **Generation:** Subject prefix + Grade + Random letter  
- **Uniqueness:** Global uniqueness check during creation
- **User-Friendly:** Easy to remember and share verbally

### **Python Service Implementation:**
The actual Python code for these operations will be implemented in:
- `app/services/school_service.py`
- `app/services/teacher_service.py`
- `app/services/student_service.py`
- `app/services/course_service.py`
- `app/services/enrollment_service.py`
- `app/services/activity_service.py` (taxonomy seed: topics → subtopics → activities)
- `app/services/assignment_service.py`

---

## 📈 **Common Query Patterns**

### **1. School Operations:**
```sql
-- Get all schools
SELECT * FROM schools ORDER BY name;

-- Get school by code
SELECT * FROM schools WHERE school_code = 'LAMAR_MIDDLE';
```

### **2. User Authentication:**
```sql
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
```

### **3. School-based Queries:**
```sql
-- All teachers in a school
SELECT * FROM teachers 
WHERE school_id = '123e4567-e89b-12d3-a456-426614174000' 
ORDER BY first_name, last_name;

-- All students in a school
SELECT * FROM students 
WHERE school_id = '123e4567-e89b-12d3-a456-426614174000' 
ORDER BY first_name, last_name;

-- All courses in a school
SELECT c.*, t.first_name, t.last_name as teacher_name
FROM courses c
JOIN teachers t ON c.teacher_id = t.id
WHERE c.school_id = '123e4567-e89b-12d3-a456-426614174000'
ORDER BY c.course_name;
```

### **4. Teacher Operations:**
```sql
-- Get all courses for a teacher
SELECT * FROM courses
WHERE teacher_id = '456e7890-e89b-12d3-a456-426614174001'
ORDER BY course_name;

-- Get teacher's student count across all courses
SELECT 
    t.first_name,
    t.last_name,
    COUNT(e.student_id) as total_students
FROM teachers t
JOIN courses c ON t.id = c.teacher_id
JOIN enrollments e ON c.id = e.course_id
WHERE t.id = '456e7890-e89b-12d3-a456-426614174001'
AND e.enrollment_status = 'active'
GROUP BY t.id, t.first_name, t.last_name;
```

### **5. Course & Enrollment Operations:**
```sql
-- Get all students in a course
SELECT s.*, e.enrolled_at, e.enrollment_status
FROM students s
JOIN enrollments e ON s.id = e.student_id
WHERE e.course_id = 'abc1234d-e89b-12d3-a456-426614174003'
AND e.enrollment_status = 'active'
ORDER BY s.first_name, s.last_name;

-- Get all courses for a student
SELECT c.*, t.first_name, t.last_name as teacher_name, e.enrollment_status
FROM courses c
JOIN teachers t ON c.teacher_id = t.id
JOIN enrollments e ON c.id = e.course_id
WHERE e.student_id = '789e0123-e89b-12d3-a456-426614174002'
ORDER BY c.course_name;

-- Find course by join code (for student enrollment)
SELECT c.*, t.first_name, t.last_name as teacher_name, s.name as school_name
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
```

---

## 🚀 **Key Benefits of PostgreSQL Design**

✅ **ACID Compliance:** Full transaction support across related entities  
✅ **Referential Integrity:** Foreign key constraints ensure data consistency  
✅ **Flexible Queries:** Complex JOINs and analytical queries supported  
✅ **Scalable:** Proven performance for educational platforms  
✅ **Standard SQL:** Familiar syntax and tooling ecosystem  
✅ **Generated Columns:** Computed fields like `full_name` for performance  
✅ **Sequences/Functions:** Clean ID generation per school scope

---

## � **Common Query Patterns**

**All SQL queries are available in `sql/03_queries.sql`**

### **Core Operations:**
- **School Management:** Find schools, get school details
- **User Authentication:** Login by email for teachers/students  
- **School-Scoped Queries:** All teachers/students/courses in a school
- **Teacher Dashboard:** Teacher's courses and student counts
- **Student Dashboard:** Student's enrolled courses and grades
- **Course Management:** Student rosters, enrollment tracking

### **Join Code Operations:**
- **Find course by code:** Student enters 'ALG7M' to find course
- **Teacher's Codes:** Display all join codes for sharing
- **Enrollment Validation:** Check if student already enrolled

### **Analytics & Reporting:**
- **School Statistics:** Enrollment counts, teacher loads
- **Popular Subjects:** Most enrolled courses
- **Grade Distribution:** Students per grade level
- **Enrollment Trends:** Active vs dropped student