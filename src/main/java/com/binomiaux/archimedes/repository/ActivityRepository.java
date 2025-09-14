package com.binomiaux.archimedes.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.config.aws.DynamoDbProperties;
import com.binomiaux.archimedes.model.Activity;
import com.binomiaux.archimedes.repository.util.DynamoKeyBuilder;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class ActivityRepository {

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Autowired
    private DynamoDbProperties dynamoDbProperties;

    public Activity findByCode(String exerciseCode) {
        String pk = DynamoKeyBuilder.buildActivityKey(exerciseCode);
        DynamoDbTable<Activity> exerciseTable = enhancedClient.table(dynamoDbProperties.getTableName(), Activity.TABLE_SCHEMA);
        DynamoDbIndex<Activity> index = exerciseTable.index("gsi1");
        Key key = Key.builder().partitionValue(pk).build();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);
        SdkIterable<Page<Activity>> pages = index.query(queryConditional);

        Optional<Activity> record = StreamSupport.stream(pages.spliterator(), false)
            .flatMap(page -> page.items().stream())
            .findFirst();

        if (!record.isPresent()) {
            return null;
        }

        return record.get();
    }

    public List<Activity> findByTopic(String topicId) {
        String pk = DynamoKeyBuilder.buildTopicKey(topicId);
        DynamoDbTable<Activity> activityTable = enhancedClient.table(dynamoDbProperties.getTableName(), Activity.TABLE_SCHEMA);
        DynamoDbIndex<Activity> index = activityTable.index("gsi2");
        Key key = Key.builder().partitionValue(pk).build();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);
        SdkIterable<Page<Activity>> pages = index.query(queryConditional);

        return StreamSupport.stream(pages.spliterator(), false)
                .flatMap(page -> page.items().stream())
                
                .collect(Collectors.toList());
    }

    public List<Activity> findByTopicAndSubtopic(String topicId, String subtopicId) {
        String pk = DynamoKeyBuilder.buildTopicSubtopicKey(topicId, subtopicId);
        DynamoDbTable<Activity> activityTable = enhancedClient.table(dynamoDbProperties.getTableName(), Activity.TABLE_SCHEMA);
        Key key = Key.builder().partitionValue(pk).build();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);
        SdkIterable<Page<Activity>> pages = activityTable.query(queryConditional);
    
        // Use Java Streams to process the results
        return StreamSupport.stream(pages.spliterator(), false)
                .flatMap(page -> page.items().stream())
                
                .collect(Collectors.toList());
    }
}
