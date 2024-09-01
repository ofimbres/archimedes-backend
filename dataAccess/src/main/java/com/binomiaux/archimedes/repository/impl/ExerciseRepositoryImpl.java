package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.api.ExerciseRepository;
import com.binomiaux.archimedes.repository.converter.ExerciseEntityTransform;
import com.binomiaux.archimedes.repository.entities.ExerciseEntity;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import com.binomiaux.archimedes.model.Exercise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class ExerciseRepositoryImpl implements ExerciseRepository {

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    private ExerciseEntityTransform transformer = new ExerciseEntityTransform();

    @Override
    public Exercise findByCode(String exerciseCode) {
        String pk = "EXERCISE#" + exerciseCode;
        DynamoDbTable<ExerciseEntity> exerciseTable = enhancedClient.table(tableName, ExerciseEntity.TABLE_SCHEMA);
        DynamoDbIndex<ExerciseEntity> index = exerciseTable.index("gsi1");
        Key key = Key.builder().partitionValue(pk).build();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);
        SdkIterable<Page<ExerciseEntity>> pages = index.query(queryConditional);

        Optional<ExerciseEntity> record = StreamSupport.stream(pages.spliterator(), false)
            .flatMap(page -> page.items().stream())
            .findFirst();

        if (!record.isPresent()) {
            return null;
        }

        return transformer.transform(record.get());
    }

    @Override
    public List<Exercise> findByTopic(String topicId) {
        String pk = "TOPIC#" + topicId;
        DynamoDbTable<ExerciseEntity> exerciseTable = enhancedClient.table("dev-archimedes-table", ExerciseEntity.TABLE_SCHEMA);
        DynamoDbIndex<ExerciseEntity> index = exerciseTable.index("gsi2");
        Key key = Key.builder().partitionValue(pk).build();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);
        SdkIterable<Page<ExerciseEntity>> pages = index.query(queryConditional);

        return StreamSupport.stream(pages.spliterator(), false)
                .flatMap(page -> page.items().stream())
                .map(transformer::transform)
                .collect(Collectors.toList());
    }

    @Override
    public List<Exercise> findByTopicAndSubtopic(String topicId, String subtopicId) {
        String pk = "TOPIC#" + topicId + "#SUBTOPIC#" + subtopicId;
        DynamoDbTable<ExerciseEntity> exerciseTable = enhancedClient.table("dev-archimedes-table", ExerciseEntity.TABLE_SCHEMA);
        Key key = Key.builder().partitionValue(pk).build();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);
        SdkIterable<Page<ExerciseEntity>> pages = exerciseTable.query(queryConditional);
    
        // Use Java Streams to process the results
        return StreamSupport.stream(pages.spliterator(), false)
                .flatMap(page -> page.items().stream())
                .map(transformer::transform)
                .collect(Collectors.toList());
    }
}
