package com.binomiaux.archimedes.repository.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.binomiaux.archimedes.model.Topic;
import com.binomiaux.archimedes.repository.TopicRepository;
import com.binomiaux.archimedes.repository.schema.TopicRecord;
import com.binomiaux.archimedes.repository.transform.TopicRecordTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class TopicRepositoryImpl implements TopicRepository {

    @Autowired
    private DynamoDBMapper mapper;

    private TopicRecordTransformer transformer = new TopicRecordTransformer();

    @Override
    public List<Topic> findAll() {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":v1",new AttributeValue().withS("TOPIC"));

        DynamoDBQueryExpression<TopicRecord> queryExpression =
                new DynamoDBQueryExpression<TopicRecord>()
                        .withKeyConditionExpression("pk = :v1")
                        .withExpressionAttributeValues(eav);

        List<TopicRecord> queryResult = mapper.query(TopicRecord.class, queryExpression);
        return queryResult.stream().map(r -> transformer.transform(r))
                .collect(Collectors.toList());
    }

    @Override
    public List<Topic> findByTopicId(String topicId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":v1",new AttributeValue().withS("TOPIC#" + topicId));

        DynamoDBQueryExpression<TopicRecord> queryExpression =
                new DynamoDBQueryExpression<TopicRecord>()
                        .withKeyConditionExpression("pk = :v1")
                        .withExpressionAttributeValues(eav);

        List<TopicRecord> queryResult = mapper.query(TopicRecord.class, queryExpression);
        return queryResult.stream().map(r -> transformer.transform(r))
                .collect(Collectors.toList());
    }
}
