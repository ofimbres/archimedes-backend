# Single Table DynamoDB Schema Design

## 🎯 **Single Table Design with Standardized GSI Convention**

**Table Name:** `archimedes-main-table`

**GSI Convention:**
- **GSI1:** `gsi1pk` / `gsi1sk` - **Relationship/Hierarchy Queries**
- **GSI2:** `gsi2pk` / `gsi2sk` - **Search/Lookup Queries**

### Counter (School-Scoped ID Generation Management)
```
pk: COUNTER#SCHOOL#SCH001                    # Main table partition key
sk: #METADATA                                # Main table sort key
entityType: COUNTER
teacherCounter: 23                           # SCH001's teacher counter
studentCounter: 156                          # SCH001's student counter
periodCounter: 42                            # SCH001's period counter (school-scoped)
enrollmentCounter: 89                        # SCH001's enrollment counter
lastUpdated: 2024-08-25T10:30:00Z
gsi1pk: null                                 # No GSI1 for counters
gsi1sk: null
gsi2pk: null                                 # No GSI2 for counters  
gsi2sk: null
```

#### School
```
pk: SCHOOL#SCH001                            # Main table partition key
sk: #METADATA                                # Main table sort key
entityType: SCHOOL
schoolId: SCH001
schoolCode: LAMAR_MIDDLE
name: LAMAR MIDDLE
address: 1818 N ARKANSAS AVE, LAREDO, TX, 78043-3097
principalName: MR EDUARDO LOPEZ
contactEmail: elopez2@Laredoisd.org
phoneNumber: (956) 273-6200
gsi1pk: SCHOOL                               # GSI1: Query all schools
gsi1sk: SCHOOL#SCH001                        # GSI1: Specific school
gsi2pk: SCHOOL_CODE                          # GSI2: Query by school code
gsi2sk: LAMAR_MIDDLE                         # GSI2: School code value
```

#### Student (School-Scoped Design)
```
pk: STUDENT#SCH001#S001                      # Main table partition key
sk: #METADATA                                # Main table sort key
entityType: STUDENT
studentId: S001                              # Local student number within school (S001, S002, etc.)
schoolId: SCH001                            # Which school this student belongs to
firstName: Diego
lastName: Hinojosa
fullName: Diego Hinojosa                     # Computed for easy sorting/display
email: diego.hinojosa@gmail.com
username: diego.hinojosa
gsi1pk: SCHOOL#SCH001                        # GSI1: Query all students by school
gsi1sk: STUDENT#S001                         # GSI1: Specific student in school
gsi2pk: EMAIL                                # GSI2: Query student by email (auth)
gsi2sk: diego.hinojosa@gmail.com             # GSI2: Email value for lookup
```

#### Teacher (School-Scoped Design)
```
pk: TEACHER#SCH001#T001                      # Main table partition key
sk: #METADATA                                # Main table sort key
entityType: TEACHER
teacherId: T001                              # Local teacher number within school (T001, T002, etc.)
schoolId: SCH001                            # Which school this teacher belongs to
firstName: Guadalupe
lastName: Trevino
fullName: Guadalupe Trevino                  # Computed for easy sorting/display
email: guadalupe.trevino@gmail.com
username: guadalupe.trevino
maxPeriods: 6
gsi1pk: SCHOOL#SCH001                        # GSI1: Query all teachers by school
gsi1sk: TEACHER#T001                         # GSI1: Specific teacher in school
gsi2pk: EMAIL                                # GSI2: Query teacher by email (auth)
gsi2sk: guadalupe.trevino@gmail.com          # GSI2: Email value for lookup
```

#### Period (Simplified School-Scoped Design)
```
pk: PERIOD#SCH001#P001                       # Main table partition key
sk: #METADATA                                # Main table sort key
entityType: PERIOD
periodId: P001                               # Local period number within school (P001, P002, etc.)
schoolId: SCH001                            # Which school this period belongs to
teacherId: T001                             # Teacher assigned to this period (reference field)
periodNumber: 6
name: Algebra I
teacherFirstName: Guadalupe                  # Denormalized for display
teacherLastName: Trevino                     # Denormalized for display
teacherFullName: Ms. Guadalupe Trevino       # Denormalized for display
gsi1pk: TEACHER#SCH001#T001                  # GSI1: Query periods by teacher
gsi1sk: PERIOD#P001                          # GSI1: Sort periods for teacher
gsi2pk: SCHOOL#SCH001                        # GSI2: Query all periods in school
gsi2sk: PERIOD#P001                          # GSI2: Sort periods in school
```

#### Student Enrollment (Simplified Design)
```
pk: STUDENT#SCH001#S001                      # Main table partition key
sk: PERIOD#P001                              # Main table sort key
entityType: ENROLLMENT
studentId: S001                              # Local student ID within school
periodId: P001                               # Local period ID within school
schoolId: SCH001                            # Which school this belongs to
studentFullName: Diego Hinojosa             # Denormalized for display
periodName: Algebra I                        # Denormalized for display
periodNumber: 6                              # Denormalized for display
teacherFullName: Ms. Guadalupe Trevino       # Denormalized for display
enrollmentDate: 2024-07-25                  # When student enrolled
status: ACTIVE                              # ACTIVE, DROPPED, COMPLETED
gsi1pk: PERIOD#SCH001#P001                   # GSI1: Query students in period
gsi1sk: STUDENT#S001                         # GSI1: Sort students in period
gsi2pk: SCHOOL#SCH001                        # GSI2: Query all enrollments in school
gsi2sk: ENROLLMENT#S001#P001                 # GSI2: Sort by student-period combo
```

#### Topic
```
pk: TOPIC#ALGEBRAIC_EXPRESSIONS             # Main table partition key
sk: #METADATA                                # Main table sort key
entityType: TOPIC
topicId: ALGEBRAIC_EXPRESSIONS
name: Algebraic Expressions
parentTopicId: null                          # null for root topics
path: Algebraic Expressions
level: 1                                     # Hierarchy level
gsi1pk: TOPIC                                # GSI1: Query all topics
gsi1sk: TOPIC#ALGEBRAIC_EXPRESSIONS          # GSI1: Specific topic
gsi2pk: ROOT_TOPIC                           # GSI2: Query root topics only
gsi2sk: TOPIC#ALGEBRAIC_EXPRESSIONS          # GSI2: Specific root topic
```

#### Topic-Subtopic Combination
```
pk: TOPIC#ALGEBRAIC_EXPRESSIONS#SUBTOPIC#AX1 # Main table partition key
sk: #METADATA                                # Main table sort key
entityType: SUBTOPIC
topicId: ALGEBRAIC_EXPRESSIONS
subtopicId: AX1
name: Simplifying Expressions
parentTopicId: ALGEBRAIC_EXPRESSIONS
path: Algebraic Expressions > Simplifying Expressions
level: 2
gsi1pk: TOPIC#ALGEBRAIC_EXPRESSIONS          # GSI1: Query subtopics by topic
gsi1sk: SUBTOPIC#AX1                         # GSI1: Specific subtopic
gsi2pk: null                                 # GSI2: No lookup needed for subtopics
gsi2sk: null
```

#### Exercise
```
pk: EXERCISE#WN16                            # Main table partition key
sk: #METADATA                                # Main table sort key
entityType: EXERCISE
exerciseId: WN16
topicId: ALGEBRAIC_EXPRESSIONS
subtopicId: AX1                              # Links to topic-subtopic
name: Multiplying Whole Numbers
exerciseType: Miniquiz                       # Miniquiz, Worksheet, Test, etc.
path: WN16.htm
difficulty: BEGINNER                         # BEGINNER, INTERMEDIATE, ADVANCED
gsi1pk: TOPIC#ALGEBRAIC_EXPRESSIONS#SUBTOPIC#AX1 # GSI1: Query exercises by topic-subtopic
gsi1sk: EXERCISE#WN16                        # GSI1: Specific exercise
gsi2pk: EXERCISE_TYPE                        # GSI2: Query exercises by type
gsi2sk: Miniquiz#WN16                        # GSI2: Type + exercise combo
```

#### Assignment (Exercise assigned to Period)
```
pk: PERIOD#SCH001#P001                       # Main table partition key
sk: ASSIGNMENT#WN16#2024-07-25               # Main table sort key (Assignment + Exercise + Date)
entityType: ASSIGNMENT
periodId: P001                               # Simplified period ID
exerciseId: WN16
assignmentDate: 2024-07-25T12:53:14.924Z
dueDate: 2024-07-30T23:59:59.000Z
name: Multiplying Whole Numbers              # Denormalized from Exercise
exerciseType: Miniquiz                       # Denormalized from Exercise
path: WN16.htm                               # Denormalized from Exercise
status: ACTIVE                               # ACTIVE, COMPLETED, OVERDUE
gsi1pk: EXERCISE#WN16                        # GSI1: Query periods assigned this exercise
gsi1sk: PERIOD#SCH001#P001                   # GSI1: Which periods have this exercise
gsi2pk: SCHOOL#SCH001                        # GSI2: Query all assignments in school
gsi2sk: ASSIGNMENT#P001#WN16                 # GSI2: Sort by period-exercise combo
```

## 🎯 **Common Query Patterns**

### **Using Main Table:**
```java
// Direct entity access
GET pk="STUDENT#SCH001#S001", sk="#METADATA"                    // Get specific student
GET pk="PERIOD#SCH001#P001", sk="#METADATA"                     // Get specific period
GET pk="STUDENT#SCH001#S001", sk begins_with "PERIOD#"          // Get student's periods

// Period assignments
GET pk="PERIOD#SCH001#P001", sk begins_with "ASSIGNMENT#"       // Get period assignments
```

### **Using GSI1 (Relationship/Hierarchy Queries):**
```java
// School-based queries
QUERY GSI1 gsi1pk="SCHOOL#SCH001", gsi1sk begins_with "STUDENT#"   // All students in school
QUERY GSI1 gsi1pk="SCHOOL#SCH001", gsi1sk begins_with "TEACHER#"   // All teachers in school

// Teacher-based queries  
QUERY GSI1 gsi1pk="TEACHER#SCH001#T001"                            // All periods for teacher

// Period-based queries
QUERY GSI1 gsi1pk="PERIOD#SCH001#P001"                             // All students in period

// Content hierarchy
QUERY GSI1 gsi1pk="TOPIC#ALGEBRAIC_EXPRESSIONS"                    // All subtopics
QUERY GSI1 gsi1pk="TOPIC#ALGEBRAIC_EXPRESSIONS#SUBTOPIC#AX1"       // All exercises
```

### **Using GSI2 (Search/Lookup Queries):**
```java
// Authentication/lookup
QUERY GSI2 gsi2pk="EMAIL", gsi2sk="diego@email.com"                // Find user by email
QUERY GSI2 gsi2pk="SCHOOL_CODE", gsi2sk="LAMAR_MIDDLE"             // Find school by code

// Content searches
QUERY GSI2 gsi2pk="EXERCISE_TYPE", gsi2sk begins_with "Miniquiz"    // All miniquiz exercises
QUERY GSI2 gsi2pk="ROOT_TOPIC"                                     // All root topics

// School-wide queries
QUERY GSI2 gsi2pk="SCHOOL#SCH001"                                  // All periods/enrollments/assignments in school
```

## 🏗️ **GSI Design Principles**

### **GSI1 - Relationship/Hierarchy Queries:**
- **Purpose:** Query related entities, navigate hierarchies
- **Pattern:** `Parent Entity → Child Entities`
- **Examples:** School→Students, Teacher→Periods, Period→Students

### **GSI2 - Search/Lookup Queries:**  
- **Purpose:** Find entities by attributes, cross-entity searches
- **Pattern:** `Attribute Type → Attribute Value`
- **Examples:** Email→User, SchoolCode→School, ExerciseType→Exercises

## 🚀 **Single Table Benefits**

✅ **ACID Transactions:** Related entities in same table support transactions  
✅ **Hot Partitions Avoided:** Data distributed across many partition keys  
✅ **Cost Effective:** One table = lower costs than multiple tables  
✅ **Query Efficiency:** Related data co-located, fewer round trips  
✅ **Flexible Schema:** Add new entity types without table changes  
✅ **Consistent Performance:** Predictable read/write patterns
