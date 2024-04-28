package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.ExerciseRepository;
import com.binomiaux.archimedes.repository.converter.ExerciseEntityTransform;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import com.binomiaux.archimedes.model.dynamodb.ExerciseEntity;
import com.binomiaux.archimedes.model.pojo.Exercise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ExerciseRepositoryImpl implements ExerciseRepository {

    @Autowired
    private DynamoDbTable<ExerciseEntity> exerciseTable;

    private ExerciseEntityTransform transformer = new ExerciseEntityTransform();

    @Override
    public Exercise findByCode(String exerciseCode) {
        QueryConditional query = QueryConditional.keyEqualTo(b -> b.partitionValue("EXERCISE#" + exerciseCode));
        PageIterable<ExerciseEntity> pagedResults = PageIterable.create(exerciseTable.index("gsi1").query(query));

        return transformer.transform(pagedResults.items().stream().toList().get(0));
    }

    @Override
    public List<Exercise> findByTopic(String topicId) {
        QueryConditional query = QueryConditional.keyEqualTo(b -> b.partitionValue("TOPIC#" + topicId));
        PageIterable<ExerciseEntity> pagedResults = PageIterable.create(exerciseTable.index("gsi2").query(query));

        return pagedResults.items().stream()
                .map(r -> transformer.transform(r))
                .collect(Collectors.toList());
    }

    @Override
    public List<Exercise> findByTopicAndSubtopic(String topicId, String subtopicId) {
        QueryConditional query = QueryConditional.keyEqualTo(b -> b.partitionValue("TOPIC#" + topicId + "#SUBTOPIC#" + subtopicId));
        PageIterable<ExerciseEntity> pagedResults = exerciseTable.query(query);

        return pagedResults.items().stream()
                .map(r -> transformer.transform(r))
                .collect(Collectors.toList());
    }
}
