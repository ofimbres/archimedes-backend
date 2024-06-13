package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.model.Topic;
import com.binomiaux.archimedes.repository.TopicRepository;
import com.binomiaux.archimedes.repository.schema.TopicRecord;
import com.binomiaux.archimedes.repository.converter.TopicRecordTransformer;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TopicRepositoryImpl implements TopicRepository {

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    private TopicRecordTransformer transformer = new TopicRecordTransformer();

    @Override
    public List<Topic> findAll() {
        DynamoDbTable<TopicRecord> topicTable = enhancedClient.table(tableName, TopicRecord.TABLE_SCHEMA);

        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue("TOPIC").build());

        List<TopicRecord> topicRecords = topicTable.query(queryConditional).items().stream().collect(Collectors.toList());

        return topicRecords.stream().map(transformer::transform).collect(Collectors.toList());
    }

    @Override
    public List<Topic> findByTopicId(String topicId) {
        DynamoDbTable<TopicRecord> topicTable = enhancedClient.table("dev-archimedes-table", TopicRecord.TABLE_SCHEMA);

        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue("TOPIC#" + topicId).build());

        List<TopicRecord> topicRecords = topicTable.query(queryConditional).items().stream().collect(Collectors.toList());

        return topicRecords.stream().map(transformer::transform).collect(Collectors.toList());
    }
}
