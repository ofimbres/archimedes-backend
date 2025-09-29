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
School (1) ←→ (Many) Period
Teacher (1) ←→ (Many) Period
Period (Many) ←→ (Many) Student (via Enrollment)
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

## 📚 **4. Classes Table**

**Purpose:** Represent classes/courses taught by teachers that students can join.

**Key Fields:**
- `id` (UUID) - Global unique identifier
- `school_id` (UUID) - Links to school (foreign key)  
- `teacher_id` (UUID) - Links to teacher (foreign key)
- `class_name` - Displayable class name (e.g., "Algebra I", "English Literature")
- `subject` - Subject category (Mathematics, English, Science, etc.)
- `join_code` - **Easy 5-6 character code for student enrollment** (e.g., 'ALG7M')
- `academic_year` - School year (e.g., '2024-25')
- `semester` - Duration (Fall, Spring, Full Year)
- `is_active` - Soft delete flag

**Join Code System:**
- **Purpose:** Simple way for students to join classes
- **Format:** Random memorable codes (ALG7M, ENG8A, SCI6B, MATH9)
- **Benefits:** Easy to remember, share, and type
- **Uniqueness:** Globally unique across all schools
- **Generation:** Random but can incorporate subject hints

---

## 🎓 **5. Enrollments Table** 

**Purpose:** Many-to-many relationship connecting students with classes.

**Key Fields:**
- `id` (UUID) - Global unique identifier
- `student_id` (UUID) - Links to student (foreign key)
- `class_id` (UUID) - Links to class (foreign key)
- `enrolled_at` (TIMESTAMP) - When student joined the class
- `enrollment_status` - Status (active, dropped, completed)
- `is_active` - Soft delete flag

**Business Logic:**
- Students join classes using the class's `join_code`
- One student can be in multiple classes
- One class can have multiple students  
- Track enrollment history and status changes

---

## 🔧 **6. Implementation Strategy**

### **ID Generation Approach:**
- **Application-Level Counters:** Handle ID generation in Python services
- **School-Scoped Numbers:** Teacher/Student/Period numbers unique per school
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
- `app/services/class_service.py`
- `app/services/enrollment_service.py`

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

-- All classes in a school
SELECT c.*, t.first_name, t.last_name as teacher_name
FROM classes c
JOIN teachers t ON c.teacher_id = t.id
WHERE c.school_id = '123e4567-e89b-12d3-a456-426614174000'
ORDER BY c.class_name;
```

### **4. Teacher Operations:**
```sql
-- Get all classes for a teacher
SELECT * FROM classes 
WHERE teacher_id = '456e7890-e89b-12d3-a456-426614174001'
ORDER BY class_name;

-- Get teacher's student count across all classes
SELECT 
    t.first_name,
    t.last_name,
    COUNT(e.student_id) as total_students
FROM teachers t
JOIN classes c ON t.id = c.teacher_id
JOIN enrollments e ON c.id = e.class_id
WHERE t.id = '456e7890-e89b-12d3-a456-426614174001'
AND e.enrollment_status = 'active'
GROUP BY t.id, t.first_name, t.last_name;
```

### **5. Class & Enrollment Operations:**
```sql
-- Get all students in a class
SELECT s.*, e.enrolled_at, e.enrollment_status
FROM students s
JOIN enrollments e ON s.id = e.student_id
WHERE e.class_id = 'abc1234d-e89b-12d3-a456-426614174003'
AND e.enrollment_status = 'active'
ORDER BY s.first_name, s.last_name;

-- Get all classes for a student
SELECT c.*, t.first_name, t.last_name as teacher_name, e.enrollment_status
FROM classes c
JOIN teachers t ON c.teacher_id = t.id
JOIN enrollments e ON c.id = e.class_id
WHERE e.student_id = '789e0123-e89b-12d3-a456-426614174002'
ORDER BY c.class_name;

-- Find class by join code (for student enrollment)
SELECT c.*, t.first_name, t.last_name as teacher_name, s.name as school_name
FROM classes c
JOIN teachers t ON c.teacher_id = t.id  
JOIN schools s ON c.school_id = s.id
WHERE c.join_code = 'ALG7M';

-- Get teacher's join codes (for sharing with students)
SELECT c.class_name, c.join_code, c.subject
FROM classes c
WHERE c.teacher_id = '456e7890-e89b-12d3-a456-426614174001'
AND c.is_active = TRUE
ORDER BY c.class_name;
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
- **School-Scoped Queries:** All teachers/students/periods in a school
- **Teacher Dashboard:** Teacher's periods and student counts
- **Student Dashboard:** Student's enrolled periods and grades
- **Class Management:** Student rosters, enrollment tracking

### **Join Code Operations:**
- **Find Period by Code:** Student enters 'ALG7M' to find class
- **Teacher's Codes:** Display all join codes for sharing
- **Enrollment Validation:** Check if student already enrolled

### **Analytics & Reporting:**
- **School Statistics:** Enrollment counts, teacher loads
- **Popular Subjects:** Most enrolled courses
- **Grade Distribution:** Students per grade level
- **Enrollment Trends:** Active vs dropped students
