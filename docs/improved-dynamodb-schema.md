# Improved DynamoDB Schema Design

## Core Entities

### Counter (ID Generation Management)
```
PK: COUNTER#SCHOOL#1234
SK: #METADATA
TYPE: COUNTER
TeacherCounter: 23                     # Current highest teacher number
StudentCounter: 156                    # Current highest student number
PeriodCounter: 45                      # Current highest period number
EnrollmentCounter: 892                 # Current highest enrollment number
LastUpdated: 2024-08-25T10:30:00Z
GSI1PK: null
GSI1SK: null
GSI2PK: null
GSI2SK: null
```


## 🔢 ID Generation Strategy

### **Atomic Counter Approach (Recommended)**

Use DynamoDB's atomic increment operations to generate sequential IDs:

#### **Counter Entity Structure:**
- **Per School**: Each school has its own counter record
- **Multiple Counters**: Separate counters for teachers, students, periods, enrollments
- **Atomic Operations**: Thread-safe increment operations
- **Fallback**: UUID generation if counter fails

#### **ID Format Examples:**
```
Teachers: T001, T002, T003, T004...
Students: S001, S002, S003, S004...
Periods:  P001, P002, P003, P004...
```

#### **Java Implementation:**
```java
@Service
public class IdGeneratorService {
    
    private final DynamoDbEnhancedClient dynamoDbClient;
    private final DynamoDbTable<Counter> countersTable;
    
    public String generateTeacherId(String schoolId) {
        return generateId(schoolId, "teacher", "T");
    }
    
    public String generateStudentId(String schoolId) {
        return generateId(schoolId, "student", "S");
    }
    
    public String generatePeriodId(String schoolId) {
        return generateId(schoolId, "period", "P");
    }
    
    private String generateId(String schoolId, String type, String prefix) {
        String counterKey = "COUNTER#SCHOOL#" + schoolId;
        String attributeName = type + "Counter";
        
        try {
            UpdateItemEnhancedRequest<Counter> request = UpdateItemEnhancedRequest.builder(Counter.class)
                .key(Key.builder()
                    .partitionValue(counterKey)
                    .sortValue("#METADATA")
                    .build())
                .updateExpression(UpdateExpression.builder()
                    .addClause("ADD " + attributeName + " :inc")
                    .addClause("SET #type = :type, LastUpdated = :timestamp")
                    .putExpressionName("#type", "TYPE")
                    .putExpressionValue(":inc", AttributeValue.builder().n("1").build())
                    .putExpressionValue(":type", AttributeValue.builder().s("COUNTER").build())
                    .putExpressionValue(":timestamp", AttributeValue.builder().s(Instant.now().toString()).build())
                    .build())
                .returnValue(ReturnValue.ALL_NEW)
                .build();
            
            Counter result = countersTable.updateItem(request).attributes();
            int newId = getCounterValue(result, type);
            
            return String.format("%s%03d", prefix, newId);
            
        } catch (Exception e) {
            // Fallback to UUID if counter fails
            return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
    
    private int getCounterValue(Counter counter, String type) {
        switch (type) {
            case "teacher": return counter.getTeacherCounter();
            case "student": return counter.getStudentCounter();
            case "period": return counter.getPeriodCounter();
            default: return 0;
        }
    }
}
```

#### **School Initialization:**
```java
public void initializeSchoolCounters(String schoolId) {
    Counter counter = new Counter();
    counter.setPartitionKey("COUNTER#SCHOOL#" + schoolId);
    counter.setSortKey("#METADATA");
    counter.setType("COUNTER");
    counter.setTeacherCounter(0);
    counter.setStudentCounter(0);
    counter.setPeriodCounter(0);
    counter.setEnrollmentCounter(0);
    counter.setLastUpdated(Instant.now().toString());
    
    countersTable.putItem(counter);
}
```

### **Benefits of This Approach:**
- ✅ **Sequential IDs**: T001, T002, T003 - easy to understand
- ✅ **No Duplicates**: Atomic operations guarantee uniqueness
- ✅ **Predictable**: Know exactly how many entities exist
- ✅ **Sortable**: Natural ordering for displays
- ✅ **Per School**: Isolated counters prevent conflicts
- ✅ **Fallback Safe**: UUID backup if counter fails

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

#### School (Final Design)
```
PK: SCHOOL#SCH001
SK: #METADATA
EntityType: SCHOOL
SchoolId: SCH001
SchoolCode: LAMAR_MIDDLE
Name: LAMAR MIDDLE
Address: 1818 N ARKANSAS AVE, LAREDO, TX, 78043-3097
PrincipalName: MR EDUARDO LOPEZ
ContactEmail: elopez2@Laredoisd.org
PhoneNumber: (956) 273-6200
ParentEntityKey: SCHOOL                # Query all schools
ChildEntityKey: SCHOOL#SCH001          # Specific school
SearchTypeKey: SCHOOL_CODE             # Query school by code
SearchValueKey: LAMAR_MIDDLE           # School code for lookup
```

#### Student (Final Design)
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
ParentEntityKey: SCHOOL#SCH001         # Query all students by school
ChildEntityKey: STUDENT#STU001         # Specific student within school
SearchTypeKey: EMAIL                   # Query student by email (auth)
SearchValueKey: diego.hinojosa@gmail.com # Email value for lookup
```

#### Teacher (Final Design)
```
PK: TEACHER#TCH001
SK: #METADATA
EntityType: TEACHER
TeacherId: TCH001
SchoolId: SCH001
FirstName: Guadalupe
LastName: Trevino
FullName: Guadalupe Trevino            # Computed for easy sorting/display
Email: guadalupe.trevino@gmail.com
Username: guadalupe.trevino
MaxPeriods: 6
ParentEntityKey: SCHOOL#SCH001         # Query all teachers by school
ChildEntityKey: TEACHER#TCH001         # Specific teacher within school
SearchTypeKey: EMAIL                   # Query teacher by email (auth)
SearchValueKey: guadalupe.trevino@gmail.com # Email value for lookup
```

#### Period (Final Design)
```
PK: PERIOD#PER001
SK: #METADATA
EntityType: PERIOD
PeriodId: PER001
SchoolId: SCH001
TeacherId: TCH001
PeriodNumber: 6
Name: Algebra I
TeacherFullName: Guadalupe Trevino     # Denormalized for display
ParentEntityKey: TEACHER#TCH001        # Query all periods by teacher
ChildEntityKey: PERIOD#PER001          # Specific period within teacher
SearchTypeKey: SCHOOL#SCH001           # Query all periods by school
SearchValueKey: PERIOD#PER001          # Specific period within school
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

#### Topic
```
PK: TOPIC#ALGEBRAIC_EXPRESSIONS
SK: #METADATA
EntityType: TOPIC
TopicId: ALGEBRAIC_EXPRESSIONS
Name: Algebraic Expressions
ParentTopicId: null                    # null for root topics
Path: Algebraic Expressions
Level: 1                               # Hierarchy level
ParentEntityKey: TOPIC                 # GSI1PK - "Get all topics"
ChildEntityKey: TOPIC#ALGEBRAIC_EXPRESSIONS # GSI1SK
SearchTypeKey: ROOT_TOPIC              # GSI2PK - "Get root topics only"
SearchValueKey: TOPIC#ALGEBRAIC_EXPRESSIONS # GSI2SK
```

#### Topic-Subtopic with Exercises (Simplified Design)
```
# Subtopic metadata
PK: TOPIC#ALGEBRAIC_EXPRESSIONS#SUBTOPIC#AX1
SK: #METADATA
EntityType: SUBTOPIC
TopicId: ALGEBRAIC_EXPRESSIONS
SubtopicId: AX1
Name: Simplifying Expressions
ParentTopicId: ALGEBRAIC_EXPRESSIONS
Path: Algebraic Expressions > Simplifying Expressions
Level: 2
ParentEntityKey: TOPIC#ALGEBRAIC_EXPRESSIONS # GSI1PK - "Get subtopics for topic"
ChildEntityKey: SUBTOPIC#AX1                 # GSI1SK
SearchTypeKey: SUBTOPIC                      # GSI2PK - "Get all subtopics"
SearchValueKey: SUBTOPIC#AX1                 # GSI2SK

# Exercise 1 for this subtopic
PK: TOPIC#ALGEBRAIC_EXPRESSIONS#SUBTOPIC#AX1
SK: EXERCISE#WN16
EntityType: EXERCISE
ExerciseId: WN16
Name: Multiplying Whole Numbers
ExerciseType: Miniquiz
Path: WN16.htm
Difficulty: BEGINNER
ParentEntityKey: TOPIC#ALGEBRAIC_EXPRESSIONS#SUBTOPIC#AX1 # GSI1PK - "Get exercises by subtopic"
ChildEntityKey: EXERCISE#WN16                            # GSI1SK
SearchTypeKey: EXERCISE_TYPE                             # GSI2PK - "Get exercises by type"
SearchValueKey: Miniquiz#WN16                            # GSI2SK

# Exercise 2 for this subtopic
PK: TOPIC#ALGEBRAIC_EXPRESSIONS#SUBTOPIC#AX1
SK: EXERCISE#WN17
EntityType: EXERCISE
ExerciseId: WN17
Name: Adding Like Terms
ExerciseType: Worksheet
Path: WN17.htm
Difficulty: INTERMEDIATE
ParentEntityKey: TOPIC#ALGEBRAIC_EXPRESSIONS#SUBTOPIC#AX1 # GSI1PK
ChildEntityKey: EXERCISE#WN17                            # GSI1SK
SearchTypeKey: EXERCISE_TYPE                             # GSI2PK
SearchValueKey: Worksheet#WN17                           # GSI2SK
```

#### Assignment (Exercise assigned to Period)
```
PK: PERIOD#PER001
SK: EXERCISE#WN16#DATE#2024-07-25
EntityType: ASSIGNMENT
PeriodId: PER001
ExerciseId: WN16
AssignmentDate: 2024-07-25T12:53:14.924Z
DueDate: 2024-07-30T23:59:59.000Z
Name: Multiplying Whole Numbers         # Denormalized from Exercise
ExerciseType: Miniquiz                  # Denormalized from Exercise
Path: WN16.htm                          # Denormalized from Exercise
Status: ACTIVE                          # ACTIVE, COMPLETED, OVERDUE
ParentEntityKey: PERIOD#PER001          # GSI1PK - "Get period assignments"
ChildEntityKey: EXERCISE#WN16           # GSI1SK
SearchTypeKey: EXERCISE#WN16            # GSI2PK - "Get exercise assignments"
SearchValueKey: ASSIGNMENT#PER001       # GSI2SK
```

#### Counter (Final Version)
```
PK: COUNTER#SCHOOL#SCH001
SK: #METADATA
EntityType: COUNTER
TeacherCounter: 23                     # Current highest teacher number
StudentCounter: 156                    # Current highest student number
PeriodCounter: 45                      # Current highest period number
EnrollmentCounter: 892                 # Current highest enrollment number
LastUpdated: 2024-08-25T10:30:00Z     # Timestamp of last update
ParentEntityKey: null                  # GSI1PK - Not needed for counters
ChildEntityKey: null                   # GSI1SK - Not needed for counters
SearchTypeKey: null                    # GSI2PK - Not needed for counters
SearchValueKey: null                   # GSI2SK - Not needed for counters
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

## Access Patterns Enabled

1. **Get all students in a period**: Query SearchTypeKey where SearchTypeKey = PERIOD#PER001
2. **Get all periods for a student**: Query ParentEntityKey where ParentEntityKey = STUDENT#STU001
3. **Get all teachers in a school**: Query ParentEntityKey where ParentEntityKey = SCHOOL#SCH001 and ChildEntityKey begins_with TEACHER#
4. **Get all periods for a teacher**: Query ParentEntityKey where ParentEntityKey = TEACHER#TCH001
5. **Find user by email**: Query SearchTypeKey where SearchTypeKey = EMAIL and SearchValueKey = email
6. **Get all students in a school**: Query ParentEntityKey where ParentEntityKey = SCHOOL#SCH001 and ChildEntityKey begins_with STUDENT#
7. **Get all schools**: Query ParentEntityKey where ParentEntityKey = SCHOOL
8. **Find school by code**: Query SearchTypeKey where SearchTypeKey = SCHOOL_CODE and SearchValueKey = school_code
9. **Get all topics**: Query ParentEntityKey where ParentEntityKey = TOPIC
10. **Get root topics only**: Query SearchTypeKey where SearchTypeKey = ROOT_TOPIC
11. **Get subtopic + all exercises**: Query main table where PK = TOPIC#TOPIC_ID#SUBTOPIC#SUBTOPIC_ID
12. **Get subtopic metadata only**: Query main table where PK = TOPIC#TOPIC_ID#SUBTOPIC#SUBTOPIC_ID and SK = #METADATA
13. **Get exercises for subtopic**: Query main table where PK = TOPIC#TOPIC_ID#SUBTOPIC#SUBTOPIC_ID and SK begins_with EXERCISE#
14. **Get exercises by type**: Query GSI2 where SearchTypeKey = EXERCISE_TYPE and SearchValueKey begins_with [type]#
13. **Get all assignments for a period**: Query main table where PK = PERIOD#PER001 and SK begins_with EXERCISE#
14. **Get all periods assigned an exercise**: Query SearchTypeKey where SearchTypeKey = EXERCISE#WN16 and SearchValueKey begins_with ASSIGNMENT#
15. **Get school counters for ID generation**: Query main table where PK = COUNTER#SCHOOL#SCH001 and SK = #METADATA
16. **Atomic increment for new IDs**: UpdateItem with ADD operation on specific counter field
17. **Get subtopic metadata**: Query main table where PK = TOPIC#TOPIC_ID#SUBTOPIC#SUBTOPIC_ID and SK = #METADATA
