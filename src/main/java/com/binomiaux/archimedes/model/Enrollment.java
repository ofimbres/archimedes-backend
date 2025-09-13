package com.binomiaux.archimedes.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Enrollment entity with improved DynamoDB design and denormalized data.
 * Stores student and period display information for efficient queries.
 */
@DynamoDbBean
public class Enrollment {
    
    // DynamoDB keys
    private String pk;                       // ENROLLMENT#ENR001
    private String sk;                       // #METADATA
    private String entityType;              // ENROLLMENT
    private String parentEntityKey;         // STUDENT#STU001 (GSI1PK)
    private String childEntityKey;          // ENROLLMENT#PER001 (GSI1SK)
    private String searchTypeKey;           // PERIOD#PER001 (GSI2PK)
    private String searchValueKey;          // ENROLLMENT#STU001 (GSI2SK)
    
    // Core enrollment fields
    private String enrollmentId;            // ENR001
    private String studentId;               // STU001
    private String periodId;                // PER001
    private String schoolId;                // SCH001 (needed for proper key generation)
    private String enrollmentDate;          // 2024-07-25
    private String status;                  // ACTIVE, DROPPED, COMPLETED
    
    // Denormalized student data (for display)
    private String studentFullName;         // "Diego Hinojosa"
    private String studentFirstName;        // "Diego"
    private String studentLastName;         // "Hinojosa"
    
    // Denormalized period data (for display)
    private String periodDisplayName;       // "Algebra I (Period 6)"
    private String periodName;              // "Algebra I"
    private String periodNumber;            // "6"
    private String teacherLastName;         // "Trevino"

    // Constructors
    public Enrollment() {}

    public Enrollment(String studentId, String periodId) {
        this.studentId = studentId;
        this.periodId = periodId;
        this.status = "ACTIVE";
    }

    public Enrollment(Student student, Period period) {
        this.studentId = student.getStudentId();
        this.periodId = period.getPeriodId();
        this.studentFullName = student.getFullName();
        this.studentFirstName = student.getFirstName();
        this.studentLastName = student.getLastName();
        this.periodName = period.getName();
        this.periodNumber = String.valueOf(period.getPeriodNumber());
        this.periodDisplayName = period.getName() + " (Period " + period.getPeriodNumber() + ")";
        this.teacherLastName = period.getTeacherLastName();
        this.status = "ACTIVE";
        this.entityType = "ENROLLMENT";
        
        generateKeys();
    }

    // DynamoDB getters/setters
    @DynamoDbPartitionKey
    @DynamoDbAttribute("pk")
    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("sk")
    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    @DynamoDbAttribute("entityType")
    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi1")
    @DynamoDbAttribute("gsi1pk")
    public String getParentEntityKey() {
        return parentEntityKey;
    }

    public void setParentEntityKey(String parentEntityKey) {
        this.parentEntityKey = parentEntityKey;
    }

    @DynamoDbSecondarySortKey(indexNames = "gsi1")
    @DynamoDbAttribute("gsi1sk")
    public String getChildEntityKey() {
        return childEntityKey;
    }

    public void setChildEntityKey(String childEntityKey) {
        this.childEntityKey = childEntityKey;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi2")
    @DynamoDbAttribute("gsi2pk")
    public String getSearchTypeKey() {
        return searchTypeKey;
    }

    public void setSearchTypeKey(String searchTypeKey) {
        this.searchTypeKey = searchTypeKey;
    }

    @DynamoDbSecondarySortKey(indexNames = "gsi2")
    @DynamoDbAttribute("gsi2sk")
    public String getSearchValueKey() {
        return searchValueKey;
    }

    public void setSearchValueKey(String searchValueKey) {
        this.searchValueKey = searchValueKey;
    }

    // Business field getters/setters
    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getPeriodId() {
        return periodId;
    }

    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(String enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStudentFullName() {
        return studentFullName;
    }

    public void setStudentFullName(String studentFullName) {
        this.studentFullName = studentFullName;
    }

    public String getStudentFirstName() {
        return studentFirstName;
    }

    public void setStudentFirstName(String studentFirstName) {
        this.studentFirstName = studentFirstName;
    }

    public String getStudentLastName() {
        return studentLastName;
    }

    public void setStudentLastName(String studentLastName) {
        this.studentLastName = studentLastName;
    }

    public String getPeriodDisplayName() {
        return periodDisplayName;
    }

    public void setPeriodDisplayName(String periodDisplayName) {
        this.periodDisplayName = periodDisplayName;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public String getPeriodNumber() {
        return periodNumber;
    }

    public void setPeriodNumber(String periodNumber) {
        this.periodNumber = periodNumber;
    }

    public String getTeacherLastName() {
        return teacherLastName;
    }

    public void setTeacherLastName(String teacherLastName) {
        this.teacherLastName = teacherLastName;
    }

    // Helper methods for key generation
    public void generateKeys() {
        // Schema pattern: PK=STUDENT#SCH001#S001, SK=PERIOD#P001
        if (this.studentId != null && this.periodId != null && this.schoolId != null) {
            this.pk = "STUDENT#" + this.schoolId + "#" + this.studentId;
            this.sk = "PERIOD#" + this.periodId;
        }
        
        // GSI1: Query students in a period (GSI1PK=PERIOD#SCH001#P001, GSI1SK=STUDENT#S001)
        if (this.periodId != null && this.studentId != null && this.schoolId != null) {
            this.parentEntityKey = "PERIOD#" + this.schoolId + "#" + this.periodId;
            this.childEntityKey = "STUDENT#" + this.studentId;
        }
        
        // GSI2: Query all enrollments in school (GSI2PK=SCHOOL#SCH001, GSI2SK=ENROLLMENT#S001#P001)
        if (this.studentId != null && this.periodId != null && this.schoolId != null) {
            this.searchTypeKey = "SCHOOL#" + this.schoolId;
            this.searchValueKey = "ENROLLMENT#" + this.studentId + "#" + this.periodId;
        }
        
        this.entityType = "ENROLLMENT";
    }

    // Helper method to get display text
    public String getDisplayText() {
        if (studentFullName != null && periodDisplayName != null) {
            return studentFullName + " enrolled in " + periodDisplayName;
        }
        return "Enrollment " + enrollmentId;
    }

    public static final TableSchema<Enrollment> TABLE_SCHEMA = TableSchema.fromBean(Enrollment.class);
}
