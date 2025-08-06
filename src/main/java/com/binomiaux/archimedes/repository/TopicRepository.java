package com.binomiaux.archimedes.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.config.aws.DynamoDbProperties;
import com.binomiaux.archimedes.model.Topic;
import com.binomiaux.archimedes.repository.util.DynamoKeyBuilder;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class TopicRepository {

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Autowired
    private DynamoDbProperties dynamoDbProperties;


    public List<Topic> findAll() {
        DynamoDbTable<Topic> topicTable = enhancedClient.table(dynamoDbProperties.getTableName(), Topic.TABLE_SCHEMA);

        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue("TOPIC").build());

        List<Topic> topicRecords = topicTable.query(queryConditional).items().stream().collect(Collectors.toList());

        return topicRecords.stream().collect(Collectors.toList());
    }

    public List<Topic> findByTopicId(String topicId) {
        DynamoDbTable<Topic> topicTable = enhancedClient.table(dynamoDbProperties.getTableName(), Topic.TABLE_SCHEMA);

        QueryConditional queryConditional = QueryConditional.keyEqualTo(
            Key.builder().partitionValue(DynamoKeyBuilder.buildTopicKey(topicId)).build()
        );

        List<Topic> topicRecords = topicTable.query(queryConditional).items().stream().collect(Collectors.toList());

        return topicRecords.stream().collect(Collectors.toList());
    }
}
