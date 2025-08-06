package com.binomiaux.archimedes.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Period entity with improved DynamoDB design and denormalized teacher data.
 */
@DynamoDbBean
public class Period {
    
    // DynamoDB keys
    private String pk;                       // PERIOD#PER001
    private String sk;                       // #METADATA
    private String entityType;              // PERIOD
    private String parentEntityKey;         // TEACHER#TCH001 (GSI1PK)
    private String childEntityKey;          // PERIOD#PER001 (GSI1SK)
    private String searchTypeKey;           // SCHOOL (GSI2PK)
    private String searchValueKey;          // SCH001#PERIOD#PER001 (GSI2SK)
    
    // Core period fields
    private String periodId;                // PER001
    private String schoolId;                // SCH001
    private String teacherId;               // TCH001
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
        
        // Set computed fields
        this.teacherFullName = "Ms. " + teacherFirstName + " " + teacherLastName;
        this.pk = "PERIOD#" + periodId;
        this.sk = "#METADATA";
        this.parentEntityKey = "TEACHER#" + teacherId;
        this.childEntityKey = "PERIOD#" + periodId;
        this.searchTypeKey = "SCHOOL";
        this.searchValueKey = schoolId + "#PERIOD#" + periodId;
    }

    // DynamoDB Primary Key
    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    // GSI1 - For querying periods by teacher
    @DynamoDbSecondaryPartitionKey(indexNames = "gsi1")
    public String getParentEntityKey() {
        return parentEntityKey;
    }

    public void setParentEntityKey(String parentEntityKey) {
        this.parentEntityKey = parentEntityKey;
    }

    @DynamoDbSecondarySortKey(indexNames = "gsi1")
    public String getChildEntityKey() {
        return childEntityKey;
    }

    public void setChildEntityKey(String childEntityKey) {
        this.childEntityKey = childEntityKey;
    }

    // GSI2 - For querying periods by school
    @DynamoDbSecondaryPartitionKey(indexNames = "gsi2")
    public String getSearchTypeKey() {
        return searchTypeKey;
    }

    public void setSearchTypeKey(String searchTypeKey) {
        this.searchTypeKey = searchTypeKey;
    }

    @DynamoDbSecondarySortKey(indexNames = "gsi2")
    public String getSearchValueKey() {
        return searchValueKey;
    }

    public void setSearchValueKey(String searchValueKey) {
        this.searchValueKey = searchValueKey;
    }

    // Entity fields
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
        if (this.periodId != null) {
            this.pk = "PERIOD#" + this.periodId;
            this.sk = "#METADATA";
            this.childEntityKey = "PERIOD#" + this.periodId;
        }
        if (this.teacherId != null) {
            this.parentEntityKey = "TEACHER#" + this.teacherId;
        }
        if (this.schoolId != null && this.periodId != null) {
            this.searchTypeKey = "SCHOOL";
            this.searchValueKey = this.schoolId + "#PERIOD#" + this.periodId;
        }
        if (this.teacherFirstName != null && this.teacherLastName != null) {
            this.teacherFullName = "Ms. " + this.teacherFirstName + " " + this.teacherLastName;
        }
        this.entityType = "PERIOD";
    }

    public static final TableSchema<Period> TABLE_SCHEMA = TableSchema.fromBean(Period.class);
}
