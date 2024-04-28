package com.binomiaux.archimedes.model.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;


@DynamoDbBean
public class TopicEntity {
    public static final TableSchema<TopicEntity> TABLE_SCHEMA = TableSchema.fromBean(TopicEntity.class);

    private String pk;
    private String sk;
    private String id;
    private String topicName;

    public TopicEntity() {
    }

    public TopicEntity(String pk, String sk, String id, String topicName) {
        this.pk = pk;
        this.sk = sk;
        this.id = id;
        this.topicName = topicName;
    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}

