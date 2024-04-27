package com.binomiaux.archimedes.repository.schema;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;


@DynamoDbBean
public class TopicRecord {
    public static final TableSchema<TopicRecord> TABLE_SCHEMA = TableSchema.fromBean(TopicRecord.class);

    private String pk;
    private String sk;
    private String id;
    private String topicName;

    public TopicRecord() {
    }

    public TopicRecord(String pk, String sk, String id, String topicName) {
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

