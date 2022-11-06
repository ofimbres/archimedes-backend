package com.binomiaux.archimedes.repository.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.binomiaux.archimedes.repository.ExerciseRepository;
import com.binomiaux.archimedes.repository.schema.ExerciseRecord;
import com.binomiaux.archimedes.repository.schema.TopicRecord;
import com.binomiaux.archimedes.repository.transform.ExerciseRecordTransform;
import com.binomiaux.archimedes.model.Exercise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ExerciseRepositoryImpl implements ExerciseRepository {

    @Autowired
    private DynamoDBMapper mapper;

    private ExerciseRecordTransform transformer = new ExerciseRecordTransform();

    @Override
    public Exercise findByCode(String exerciseCode) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":v1", new AttributeValue().withS("EXERCISE#" + exerciseCode));

        DynamoDBQueryExpression<ExerciseRecord> queryExpression =
                new DynamoDBQueryExpression<ExerciseRecord>()
                        .withKeyConditionExpression("gsipk = :v1")
                        .withExpressionAttributeValues(eav)
                        .withIndexName("gsi1")
                        .withConsistentRead(false);

        List<ExerciseRecord> queryResult = mapper.query(ExerciseRecord.class, queryExpression);
        return transformer.transform(queryResult.get(0));
    }

    @Override
    public List<Exercise> findByTopicAndSubtopic(String topicId, String subtopicId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":v1",new AttributeValue().withS("TOPIC#" + topicId + "#SUBTOPIC#" + subtopicId));

        DynamoDBQueryExpression<ExerciseRecord> queryExpression =
                new DynamoDBQueryExpression<ExerciseRecord>()
                        .withKeyConditionExpression("pk = :v1")
                        .withExpressionAttributeValues(eav);

        List<ExerciseRecord> queryResult = mapper.query(ExerciseRecord.class, queryExpression);
        return queryResult.stream().map(r -> transformer.transform(r))
                .collect(Collectors.toList());
    }
}
