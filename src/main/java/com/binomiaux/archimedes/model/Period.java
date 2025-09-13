package com.binomiaux.archimedes.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Period entity with simplified school-scoped design while maintaining teacher relationship.
 */
@DynamoDbBean
public class Period {
    
    // DynamoDB keys - simplified school-scoped design
    private String pk;                       // PERIOD#SCH001#P001
    private String sk;                       // #METADATA
    private String entityType;              // PERIOD
    
    // Core period fields
    private String periodId;                // P001
    private String schoolId;                // SCH001
    private String teacherId;               // T001 (reference field, not in key)
    private Integer periodNumber;           // 6
    private String name;                    // "Algebra I"
    
    // Denormalized teacher data (for display)
    private String teacherFirstName;        // "Guadalupe"
    private String teacherLastName;         // "Trevino"
    private String teacherFullName;         // "Ms. Guadalupe Trevino"

    // Constructors
    public Period() {}

    public Period(String periodId, String schoolId, String teacherId, Integer periodNumber, String name,
                  String teacherFirstName, String teacherLastName) {
        this.periodId = periodId;
        this.schoolId = schoolId;
        this.teacherId = teacherId;
        this.periodNumber = periodNumber;
        this.name = name;
        this.teacherFirstName = teacherFirstName;
        this.teacherLastName = teacherLastName;
        this.entityType = "PERIOD";
        
        // Set computed fields - simplified keys
        this.teacherFullName = "Ms. " + teacherFirstName + " " + teacherLastName;
        this.pk = "PERIOD#" + schoolId + "#" + periodId;
        this.sk = "#METADATA";
    }

    // DynamoDB Primary Key
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

    // GSI1 for teacher-period queries - maintains relationship efficiently
    @DynamoDbSecondaryPartitionKey(indexNames = "gsi1")
    @DynamoDbAttribute("gsi1pk")
    public String getTeacherPeriodKey() {
        if (schoolId != null && teacherId != null) {
            return "TEACHER#" + schoolId + "#" + teacherId;
        }
        return null;
    }

    public void setTeacherPeriodKey(String teacherPeriodKey) {
        // Computed field - no setter logic needed
    }

    @DynamoDbSecondarySortKey(indexNames = "gsi1")
    @DynamoDbAttribute("gsi1sk")
    public String getPeriodSortKey() {
        if (periodId != null) {
            return "PERIOD#" + periodId;
        }
        return null;
    }

    public void setPeriodSortKey(String periodSortKey) {
        // Computed field - no setter logic needed
    }

    // Entity fields
    @DynamoDbAttribute("entityType")
    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
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

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public Integer getPeriodNumber() {
        return periodNumber;
    }

    public void setPeriodNumber(Integer periodNumber) {
        this.periodNumber = periodNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacherFirstName() {
        return teacherFirstName;
    }

    public void setTeacherFirstName(String teacherFirstName) {
        this.teacherFirstName = teacherFirstName;
        // Update computed teacherFullName when firstName changes
        if (this.teacherLastName != null) {
            this.teacherFullName = "Ms. " + teacherFirstName + " " + this.teacherLastName;
        }
    }

    public String getTeacherLastName() {
        return teacherLastName;
    }

    public void setTeacherLastName(String teacherLastName) {
        this.teacherLastName = teacherLastName;
        // Update computed teacherFullName when lastName changes
        if (this.teacherFirstName != null) {
            this.teacherFullName = "Ms. " + this.teacherFirstName + " " + teacherLastName;
        }
    }

    public String getTeacherFullName() {
        return teacherFullName;
    }

    public void setTeacherFullName(String teacherFullName) {
        this.teacherFullName = teacherFullName;
    }

    // Helper methods for key generation
    public void generateKeys() {
        if (this.schoolId != null && this.periodId != null) {
            this.pk = "PERIOD#" + this.schoolId + "#" + this.periodId;
            this.sk = "#METADATA";
        }
        if (this.teacherFirstName != null && this.teacherLastName != null) {
            this.teacherFullName = this.teacherFirstName + " " + this.teacherLastName;
        }
        this.entityType = "PERIOD";
    }

    /**
     * Helper method to build partition key for repository operations.
     */
    public static String buildPartitionKey(String schoolId, String periodId) {
        return "PERIOD#" + schoolId + "#" + periodId;
    }

    /**
     * Helper method to build sort key for repository operations.
     */
    public static String buildSortKey() {
        return "#METADATA";
    }

    /**
     * Get the simple period ID for API responses.
     * Format: P001
     */
    public String getSimplePeriodId() {
        return periodId;
    }

    /**
     * Get the school-scoped period ID for internal references.
     * Format: SCH001#P001
     */
    public String getSchoolScopedPeriodId() {
        if (schoolId != null && periodId != null) {
            return schoolId + "#" + periodId;
        }
        return null;
    }

    public static final TableSchema<Period> TABLE_SCHEMA = TableSchema.fromBean(Period.class);
}
