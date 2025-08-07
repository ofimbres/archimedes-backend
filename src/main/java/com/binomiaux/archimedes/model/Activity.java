package com.binomiaux.archimedes.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Exercise (Activity) entity with improved DynamoDB design.
 * Note: This class is also known as Activity in the legacy code.
 */
@DynamoDbBean
public class Activity {
    
    // DynamoDB keys
    private String pk;                       // EXERCISE#WN16
    private String sk;                       // #METADATA
    private String entityType;              // EXERCISE
    private String parentEntityKey;         // TOPIC#ALGEBRAIC_EXPRESSIONS (GSI1PK)
    private String childEntityKey;          // EXERCISE#WN16 (GSI1SK)
    private String searchTypeKey;           // EXERCISE_TYPE (GSI2PK)
    private String searchValueKey;          // Miniquiz#WN16 (GSI2SK)
    
    // Core exercise fields
    private String exerciseId;              // WN16 (also stored as 'code' for legacy)
    private String code;                    // WN16 (legacy field name)
    private String topicId;                 // ALGEBRAIC_EXPRESSIONS
    private String name;                    // "Multiplying Whole Numbers"
    private String exerciseType;            // "Miniquiz", "Worksheet", "Test"
    private String classification;          // Legacy field for exerciseType
    private String path;                    // "WN16.htm"
    private String url;                     // Full URL if different from path
    private String difficulty;              // "BEGINNER", "INTERMEDIATE", "ADVANCED"

    // Constructors
    public Activity() {}

    // Legacy constructor
    public Activity(String activityId, String name, String classification, String path) {
        this.code = activityId;
        this.exerciseId = activityId;
        this.name = name;
        this.classification = classification;
        this.exerciseType = classification;
        this.path = path;
        this.entityType = "EXERCISE";
        this.difficulty = "BEGINNER"; // Default
        
        generateKeys();
    }

    // New constructor with topic
    public Activity(String exerciseId, String topicId, String name, String exerciseType, String path, String difficulty) {
        this.exerciseId = exerciseId;
        this.code = exerciseId; // For legacy compatibility
        this.topicId = topicId;
        this.name = name;
        this.exerciseType = exerciseType;
        this.classification = exerciseType; // For legacy compatibility
        this.path = path;
        this.difficulty = difficulty;
        this.entityType = "EXERCISE";
        
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

    // GSI1 - For querying exercises by topic
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

    // GSI2 - For querying exercises by type
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

    public String getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
        this.code = exerciseId; // Keep legacy field in sync
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        this.exerciseId = code; // Keep new field in sync
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
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
        this.classification = exerciseType; // Keep legacy field in sync
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
        this.exerciseType = classification; // Keep new field in sync
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    // Legacy GSI methods for backward compatibility
    @Deprecated
    public String getGsi1pk() {
        return parentEntityKey;
    }

    @Deprecated
    public void setGsi1pk(String gsi1pk) {
        this.parentEntityKey = gsi1pk;
    }

    @Deprecated
    public String getGsi1sk() {
        return childEntityKey;
    }

    @Deprecated
    public void setGsi1sk(String gsi1sk) {
        this.childEntityKey = gsi1sk;
    }

    @Deprecated
    public String getGsi2pk() {
        return searchTypeKey;
    }

    @Deprecated
    public void setGsi2pk(String gsi2pk) {
        this.searchTypeKey = gsi2pk;
    }

    @Deprecated
    public String getGsi2sk() {
        return searchValueKey;
    }

    @Deprecated
    public void setGsi2sk(String gsi2sk) {
        this.searchValueKey = gsi2sk;
    }

    // Helper methods
    public void generateKeys() {
        if (this.exerciseId != null) {
            this.pk = "EXERCISE#" + this.exerciseId;
            this.sk = "#METADATA";
            this.childEntityKey = "EXERCISE#" + this.exerciseId;
        }
        if (this.topicId != null) {
            this.parentEntityKey = "TOPIC#" + this.topicId;
        }
        if (this.exerciseType != null && this.exerciseId != null) {
            this.searchTypeKey = "EXERCISE_TYPE";
            this.searchValueKey = this.exerciseType + "#" + this.exerciseId;
        }
        this.entityType = "EXERCISE";
    }

    public static final TableSchema<Activity> TABLE_SCHEMA = TableSchema.fromBean(Activity.class);
}