package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.ExerciseRepository;
import com.binomiaux.archimedes.repository.schema.ExerciseRecord;
import com.binomiaux.archimedes.repository.transform.ExerciseRecordTransform;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import com.binomiaux.archimedes.model.Exercise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ExerciseRepositoryImpl implements ExerciseRepository {

    @Autowired
    private DynamoDbTable<ExerciseRecord> exerciseTable;

    private ExerciseRecordTransform transformer = new ExerciseRecordTransform();

    @Override
    public Exercise findByCode(String exerciseCode) {
        QueryConditional query = QueryConditional.keyEqualTo(b -> b.partitionValue("EXERCISE#" + exerciseCode));
        PageIterable<ExerciseRecord> pagedResults = PageIterable.create(exerciseTable.index("gsi1").query(query));

        return transformer.transform(pagedResults.items().stream().toList().get(0));
    }

    @Override
    public List<Exercise> findByTopic(String topicId) {
        QueryConditional query = QueryConditional.keyEqualTo(b -> b.partitionValue("TOPIC#" + topicId));
        PageIterable<ExerciseRecord> pagedResults = PageIterable.create(exerciseTable.index("gsi2").query(query));

        return pagedResults.items().stream()
                .map(r -> transformer.transform(r))
                .collect(Collectors.toList());
    }

    @Override
    public List<Exercise> findByTopicAndSubtopic(String topicId, String subtopicId) {
        QueryConditional query = QueryConditional.keyEqualTo(b -> b.partitionValue("TOPIC#" + topicId + "#SUBTOPIC#" + subtopicId));
        PageIterable<ExerciseRecord> pagedResults = exerciseTable.query(query);

        return pagedResults.items().stream()
                .map(r -> transformer.transform(r))
                .collect(Collectors.toList());
    }
}
