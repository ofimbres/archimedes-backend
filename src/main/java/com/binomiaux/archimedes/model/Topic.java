package com.binomiaux.archimedes.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Topic entity with improved DynamoDB design supporting hierarchical topics and subtopics.
 */
@DynamoDbBean
public class Topic {
    
    // DynamoDB keys
    private String pk;                       // TOPIC#ALGEBRAIC_EXPRESSIONS
    private String sk;                       // #METADATA or SUBTOPIC#AX1
    private String entityType;              // TOPIC or SUBTOPIC
    private String parentEntityKey;         // TOPIC or TOPIC#PARENT_ID (GSI1PK)
    private String childEntityKey;          // TOPIC#ID or SUBTOPIC#ID (GSI1SK)
    private String searchTypeKey;           // null (GSI2PK)
    private String searchValueKey;          // null (GSI2SK)
    
    // Core topic fields
    private String topicId;                 // ALGEBRAIC_EXPRESSIONS
    private String subtopicId;              // AX1 (only for subtopics)
    private String name;                    // "Algebraic Expressions"
    private String parentTopicId;           // null for root topics, parent ID for subtopics
    private String path;                    // "Algebraic Expressions" or "Algebra > Expressions"
    private Integer level;                  // 1 for topics, 2+ for subtopics

    // Constructors
    public Topic() {}

    // Constructor for root topic
    public Topic(String topicId, String name) {
        this.topicId = topicId;
        this.name = name;
        this.entityType = "TOPIC";
        this.level = 1;
        this.path = name;
        
        // Set computed fields
        this.pk = "TOPIC#" + topicId;
        this.sk = "#METADATA";
        this.parentEntityKey = "TOPIC";
        this.childEntityKey = "TOPIC#" + topicId;
    }

    // Constructor for subtopic
    public Topic(String topicId, String subtopicId, String name, String parentTopicId, String parentPath) {
        this.topicId = topicId;
        this.subtopicId = subtopicId;
        this.name = name;
        this.parentTopicId = parentTopicId;
        this.entityType = "SUBTOPIC";
        this.level = 2;
        this.path = parentPath + " > " + name;
        
        // Set computed fields
        this.pk = "TOPIC#" + topicId;
        this.sk = "SUBTOPIC#" + subtopicId;
        this.parentEntityKey = "TOPIC#" + parentTopicId;
        this.childEntityKey = "SUBTOPIC#" + subtopicId;
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

    // GSI1 - For querying topics by parent or all topics
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

    // GSI2 - Currently unused for topics
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

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getSubtopicId() {
        return subtopicId;
    }

    public void setSubtopicId(String subtopicId) {
        this.subtopicId = subtopicId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentTopicId() {
        return parentTopicId;
    }

    public void setParentTopicId(String parentTopicId) {
        this.parentTopicId = parentTopicId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getId() {
        return topicId;
    }

    public void setId(String id) {
        this.topicId = id;
    }

    // Helper methods
    public boolean isRootTopic() {
        return "TOPIC".equals(entityType) && parentTopicId == null;
    }

    public boolean isSubtopic() {
        return "SUBTOPIC".equals(entityType);
    }

    public void generateKeys() {
        if (this.topicId != null) {
            this.pk = "TOPIC#" + this.topicId;
            
            if (isSubtopic() && this.subtopicId != null) {
                this.sk = "SUBTOPIC#" + this.subtopicId;
                this.childEntityKey = "SUBTOPIC#" + this.subtopicId;
                this.parentEntityKey = "TOPIC#" + this.parentTopicId;
            } else {
                this.sk = "#METADATA";
                this.childEntityKey = "TOPIC#" + this.topicId;
                this.parentEntityKey = "TOPIC";
            }
        }
    }

    public static final TableSchema<Topic> TABLE_SCHEMA = TableSchema.fromBean(Topic.class);
}
