package com.binomiaux.archimedes.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Assignment entity representing an exercise assigned to a period with improved DynamoDB design.
 * This uses a composite key pattern where the Period is the parent and exercises are children.
 */
@DynamoDbBean
public class Assignment {
    
    // DynamoDB keys
    private String pk;                       // PERIOD#PER001
    private String sk;                       // EXERCISE#WN16#DATE#2024-07-25
    private String entityType;              // ASSIGNMENT
    private String parentEntityKey;         // PERIOD#PER001 (GSI1PK)
    private String childEntityKey;          // EXERCISE#WN16 (GSI1SK)
    private String searchTypeKey;           // EXERCISE#WN16 (GSI2PK)
    private String searchValueKey;          // ASSIGNMENT#PER001 (GSI2SK)
    
    // Core assignment fields
    private String assignmentId;            // Generated ID (optional)
    private String periodId;                // PER001
    private String exerciseId;              // WN16
    private String assignmentDate;          // 2024-07-25T12:53:14.924Z
    private String dueDate;                 // 2024-07-30T23:59:59.000Z
    private String status;                  // ACTIVE, COMPLETED, OVERDUE
    
    // Denormalized exercise data (for display)
    private String name;                    // "Multiplying Whole Numbers"
    private String exerciseType;            // "Miniquiz"
    private String path;                    // "WN16.htm"
    
    // Denormalized period data (for display)
    private String periodName;              // "Algebra I"
    private String periodNumber;            // "6"

    // Constructors
    public Assignment() {}

    public Assignment(String periodId, String exerciseId, String assignmentDate) {
        this.periodId = periodId;
        this.exerciseId = exerciseId;
        this.assignmentDate = assignmentDate;
        this.status = "ACTIVE";
        this.entityType = "ASSIGNMENT";
        
        generateKeys();
    }

    public Assignment(String periodId, String exerciseId, String assignmentDate, String dueDate,
                      String exerciseName, String exerciseType, String exercisePath) {
        this.periodId = periodId;
        this.exerciseId = exerciseId;
        this.assignmentDate = assignmentDate;
        this.dueDate = dueDate;
        this.name = exerciseName;
        this.exerciseType = exerciseType;
        this.path = exercisePath;
        this.status = "ACTIVE";
        this.entityType = "ASSIGNMENT";
        
        generateKeys();
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

    // GSI1 - For querying assignments by period
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

    // GSI2 - For querying assignments by exercise
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

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getPeriodId() {
        return periodId;
    }

    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }

    public String getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(String assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    // Helper methods
    public void generateKeys() {
        if (this.periodId != null) {
            this.pk = "PERIOD#" + this.periodId;
            this.parentEntityKey = "PERIOD#" + this.periodId;
        }
        if (this.exerciseId != null && this.assignmentDate != null) {
            // Extract just the date part if it's a full timestamp
            String dateOnly = this.assignmentDate.split("T")[0];
            this.sk = "EXERCISE#" + this.exerciseId + "#DATE#" + dateOnly;
            this.childEntityKey = "EXERCISE#" + this.exerciseId;
            this.searchTypeKey = "EXERCISE#" + this.exerciseId;
        }
        if (this.periodId != null) {
            this.searchValueKey = "ASSIGNMENT#" + this.periodId;
        }
        this.entityType = "ASSIGNMENT";
    }

    // Helper method to get display text
    public String getDisplayText() {
        if (name != null && exerciseType != null) {
            return name + " (" + exerciseType + ")";
        }
        return exerciseId;
    }

    public static final TableSchema<Assignment> TABLE_SCHEMA = TableSchema.fromBean(Assignment.class);
}
