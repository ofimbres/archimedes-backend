package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.model.Topic;
import com.binomiaux.archimedes.repository.TopicRepository;
import com.binomiaux.archimedes.repository.schema.TopicRecord;
import com.binomiaux.archimedes.repository.transform.TopicRecordTransformer;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TopicRepositoryImpl implements TopicRepository {

    @Autowired
    private DynamoDbTable<TopicRecord> topicTable;

    private TopicRecordTransformer transformer = new TopicRecordTransformer();

    @Override
    public List<Topic> findAll() {
        QueryConditional query = QueryConditional.keyEqualTo(b -> b.partitionValue("TOPIC"));
        PageIterable<TopicRecord> pagedResults = topicTable.query(query);

        return pagedResults.items().stream()
                .map(r -> transformer.transform(r))
                .collect(Collectors.toList());
    }

    @Override
    public List<Topic> findByTopicId(String topicId) {
        QueryConditional query = QueryConditional.keyEqualTo(b -> b.partitionValue("TOPIC#" + topicId));
        PageIterable<TopicRecord> pagedResults = topicTable.query(query);

        return pagedResults.items().stream()
                .map(r -> transformer.transform(r))
                .collect(Collectors.toList());
    }
}
