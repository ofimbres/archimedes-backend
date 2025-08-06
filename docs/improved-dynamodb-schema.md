# Improved DynamoDB Schema Design

## Core Entities

### School
```
PK: SCHOOL#1234
SK: #METADATA
TYPE: SCHOOL
SchoolId: 1234
Name: Lamar High School
StudentCount: 150
TeacherCount: 8
GSI1PK: SCHOOL
GSI1SK: SCHOOL#1234
```

### Teacher
```
PK: TEACHER#T001
SK: #METADATA
TYPE: TEACHER
TeacherId: T001
SchoolId: 1234
FirstName: Guadalupe
LastName: Trevino
Email: guadalupe.trevino@gmail.com
Username: guadalupe.trevino
MaxPeriods: 6
GSI1PK: SCHOOL#1234
GSI1SK: TEACHER#T001
GSI2PK: EMAIL
GSI2SK: guadalupe.trevino@gmail.com
```

### Student
```
PK: STUDENT#S001
SK: #METADATA
TYPE: STUDENT
StudentId: S001
SchoolId: 1234
FirstName: Diego
LastName: Hinojosa
Email: diego.hinojosa@gmail.com
Username: diego.hinojosa
GSI1PK: SCHOOL#1234
GSI1SK: STUDENT#S001
GSI2PK: EMAIL
GSI2SK: diego.hinojosa@gmail.com
```

### Period
```
PK: PERIOD#P001
SK: #METADATA
TYPE: PERIOD
PeriodId: P001
SchoolId: 1234
TeacherId: T001
PeriodNumber: 6
Name: Math 101
TeacherFirstName: Guadalupe
TeacherLastName: Trevino
GSI1PK: TEACHER#T001
GSI1SK: PERIOD#P001
GSI2PK: SCHOOL#1234
GSI2SK: PERIOD#P001
```

### Enrollment (Recommended Approach)
```
PK: ENROLLMENT#E001
SK: #METADATA
TYPE: ENROLLMENT
EnrollmentId: E001
StudentId: S001
PeriodId: P001
StudentFirstName: Diego
StudentLastName: Hinojosa
PeriodName: Math 101
EnrollmentDate: 2024-07-25
GSI1PK: STUDENT#S001
GSI1SK: ENROLLMENT#P001
GSI2PK: PERIOD#P001
GSI2SK: ENROLLMENT#S001
```

## Improved Naming Conventions

### 🔄 **Current → Improved Names**

#### Entity IDs (More Descriptive)
```
❌ Current: STUDENT#1234-S1     →  ✅ Better: STUDENT#STU001
❌ Current: TEACHER#1234-T1     →  ✅ Better: TEACHER#TCH001  
❌ Current: PERIOD#1234-T1-6    →  ✅ Better: PERIOD#PER001
❌ Current: ENROLLMENT#E001     →  ✅ Better: ENROLLMENT#ENR001
```

#### GSI Names (Clearer Purpose)
```
❌ Current: GSI1PK, GSI1SK      →  ✅ Better: ParentEntityPK, ChildEntitySK
❌ Current: GSI2PK, GSI2SK      →  ✅ Better: SearchTypePK, SearchValueSK
❌ Current: GSIPK1, GSISK1      →  ✅ Better: (Remove - too many GSIs)
```

#### Field Names (More Intuitive)
```
❌ Current: pk, sk              →  ✅ Better: partitionKey, sortKey
❌ Current: gsi1pk              →  ✅ Better: parentEntityKey
❌ Current: gsi2pk              →  ✅ Better: searchTypeKey
❌ Current: type                →  ✅ Better: entityType
❌ Current: id                  →  ✅ Better: entityId
```

### 🎯 **Final Recommended Schema**

#### Student
```
PK: STUDENT#STU001
SK: #METADATA
EntityType: STUDENT
StudentId: STU001
SchoolId: SCH001
FirstName: Diego
LastName: Hinojosa
FullName: Diego Hinojosa               # Computed for easy sorting/display
Email: diego.hinojosa@gmail.com
Username: diego.hinojosa
ParentEntityKey: SCHOOL#SCH001         # GSI1PK
ChildEntityKey: STUDENT#STU001         # GSI1SK  
SearchTypeKey: EMAIL                   # GSI2PK
SearchValueKey: diego.hinojosa@gmail.com # GSI2SK
```

#### Period  
```
PK: PERIOD#PER001
SK: #METADATA
EntityType: PERIOD
PeriodId: PER001
SchoolId: SCH001
TeacherId: TCH001
PeriodNumber: 6
Name: Algebra I
TeacherFullName: Ms. Guadalupe Trevino # Denormalized for display
ParentEntityKey: TEACHER#TCH001        # GSI1PK
ChildEntityKey: PERIOD#PER001          # GSI1SK
SearchTypeKey: SCHOOL                  # GSI2PK  
SearchValueKey: SCH001#PERIOD#PER001   # GSI2SK
```

#### Enrollment (Final Version)
```
PK: ENROLLMENT#ENR001
SK: #METADATA
EntityType: ENROLLMENT
EnrollmentId: ENR001
StudentId: STU001
PeriodId: PER001
StudentFullName: Diego Hinojosa
PeriodDisplayName: Algebra I (Period 6)
TeacherLastName: Trevino
EnrollmentDate: 2024-07-25
Status: ACTIVE                         # ACTIVE, DROPPED, COMPLETED
ParentEntityKey: STUDENT#STU001        # GSI1PK - "Get student's enrollments"
ChildEntityKey: ENROLLMENT#PER001      # GSI1SK
SearchTypeKey: PERIOD#PER001           # GSI2PK - "Get period's enrollments"  
SearchValueKey: ENROLLMENT#STU001      # GSI2SK
```

## Denormalization Strategy for Enrollments

### ✅ **INCLUDE These Fields (Frequently Displayed)**
```
PK: ENROLLMENT#E001
SK: #METADATA
TYPE: ENROLLMENT
EnrollmentId: E001
StudentId: S001
PeriodId: P001
# Denormalized for display - changes infrequently
StudentFirstName: Diego
StudentLastName: Hinojosa  
StudentFullName: Diego Hinojosa        # Computed field for easy display
PeriodName: Math 101
PeriodNumber: 6
TeacherLastName: Trevino               # For "Mr. Trevino's Math 101"
EnrollmentDate: 2024-07-25
GSI1PK: STUDENT#S001
GSI1SK: ENROLLMENT#P001
GSI2PK: PERIOD#P001
GSI2SK: ENROLLMENT#S001
```

### ❌ **DON'T INCLUDE These Fields (Change Frequently)**
```
# Don't store these - they change often:
StudentEmail: diego@example.com        # ❌ May change
StudentGrade: 85                       # ❌ Changes often  
TeacherEmail: teacher@example.com      # ❌ May change
PeriodStartTime: 09:00                 # ❌ May change
```

### 🎯 **Why Denormalize Display Data?**

**Benefits:**
- ✅ **Single query** to show enrollment list with names
- ✅ **Better performance** (no joins needed)
- ✅ **Lower cost** (fewer read units)
- ✅ **Simpler client code**

**Trade-offs:**
- ⚠️ **Data consistency** - name changes need updates
- ⚠️ **Storage cost** - duplicated data
- ⚠️ **Update complexity** - multiple records to update

### 📱 **Real-world Example:**
```
// Enrollment list display - Single query!
"Diego Hinojosa - Math 101 (Period 6)"
"Maria Garcia - History 102 (Period 3)" 
"John Smith - English 201 (Period 1)"
```

Without denormalization, you'd need:
1. Query enrollments
2. Query each student 
3. Query each period
= **Much more expensive and complex!**

## Query Examples with Improved Design

### 1. **Get All Students in a Period** (Single Query!)
```java
// Query GSI2: SearchTypeKey = "PERIOD#PER001"
Result: All enrollments with student names included
→ "Diego Hinojosa", "Maria Garcia", "John Smith"
```

### 2. **Get All Periods for a Student** (Single Query!)
```java  
// Query GSI1: ParentEntityKey = "STUDENT#STU001"
Result: All enrollments with period names included
→ "Algebra I (Period 6)", "English 101 (Period 2)"
```

### 3. **Display Enrollment List** (Single Query!)
```java
// Query main table: PK begins_with "ENROLLMENT#"
Result: Ready-to-display enrollment data
→ "Diego Hinojosa enrolled in Ms. Trevino's Algebra I (Period 6)"
```

### 4. **Find Student by Email** (Single Query!)
```java
// Query GSI2: SearchTypeKey = "EMAIL", SearchValueKey = "diego@gmail.com"
Result: Student record directly
→ Student#STU001 data
```

## Performance Comparison

### ❌ **Without Denormalization:**
```
1. Query enrollments (1 RCU)
2. Query each student (N RCUs) 
3. Query each period (N RCUs)
Total: 1 + 2N RCUs, 1 + 2N API calls
```

### ✅ **With Denormalization:**
```
1. Query enrollments with names (1 RCU)
Total: 1 RCU, 1 API call
```

**Result:** **2N times faster and cheaper!**

## Access Patterns Enabled

1. **Get all students in a period**: Query GSI2 where GSI2PK = PERIOD#P001
2. **Get all periods for a student**: Query GSI1 where GSI1PK = STUDENT#S001
3. **Get all teachers in a school**: Query GSI1 where GSI1PK = SCHOOL#1234 and GSI1SK begins_with TEACHER#
4. **Get all periods for a teacher**: Query GSI1 where GSI1PK = TEACHER#T001
5. **Find user by email**: Query GSI2 where GSI2PK = EMAIL and GSI2SK = email
6. **Get all students in a school**: Query GSI1 where GSI1PK = SCHOOL#1234 and GSI1SK begins_with STUDENT#

## Benefits of This Design

✅ **Efficient Queries**: All common access patterns are single-query operations
✅ **Scalable**: Each entity type has its own key space
✅ **Flexible**: Easy to add new entity types
✅ **Maintainable**: Clear naming conventions
✅ **Cost-effective**: Minimal GSI usage
