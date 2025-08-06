package com.binomiaux.archimedes.repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.model.ActivitySubmission;
import com.binomiaux.archimedes.config.aws.DynamoDbProperties;
import com.binomiaux.archimedes.model.ActivitySubmission;
import com.binomiaux.archimedes.repository.util.DynamoKeyBuilder;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

/**
 * Simplified ActivitySubmissionRepository without interface abstraction.
 * Uses DynamoKeyBuilder for consistent key generation and configuration injection.
 */
@Repository
public class ActivitySubmissionRepository {

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Autowired
    private DynamoDbProperties dynamoDbProperties;


    public ActivitySubmission find(String id) {
        DynamoDbTable<ActivitySubmission> exerciseResultTable = enhancedClient.table(dynamoDbProperties.getTableName(), ActivitySubmission.TABLE_SCHEMA);
        ActivitySubmission entity = exerciseResultTable.getItem(r -> r.key(Key.builder().partitionValue(DynamoKeyBuilder.buildActivityResultKey(id)).sortValue(DynamoKeyBuilder.METADATA_KEY).build()));

        return entity;
    }

    public void create(ActivitySubmission result) {
        // exercise score
        DynamoDbTable<ActivitySubmission> exerciseResultTable = enhancedClient.table(dynamoDbProperties.getTableName(), ActivitySubmission.TABLE_SCHEMA);
        ActivitySubmission record = new ActivitySubmission();
        String exerciseResultId = result.getActivityResultId();

        record.setPk(DynamoKeyBuilder.buildActivityResultKey(exerciseResultId));
        record.setSk(DynamoKeyBuilder.METADATA_KEY);
        record.setGsi1pk(DynamoKeyBuilder.buildPeriodStudentKey(result.getPeriod().getPeriodId(), result.getStudent().getStudentId()));
        
        LocalDate localDate = result.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = localDate.format(formatter);
        record.setGsi1sk(DynamoKeyBuilder.buildDateKey(formattedDate));

        record.setGsi2pk(DynamoKeyBuilder.buildPeriodStudentKey(result.getPeriod().getPeriodId(), result.getStudent().getStudentId()));
        record.setGsi2sk(DynamoKeyBuilder.buildActivityKey(result.getActivity().getActivityId()));

        record.setType("ACTIVITY_RESULT");

        record.setActivityResultId(exerciseResultId);
        
        record.setPath(result.getResourcePath()); 
        record.setScore(result.getScore());
        record.setTimestamp(result.getTimestamp().toString());
        record.setActivityId(result.getActivity().getActivityId());

        exerciseResultTable.putItem(record);
    }
}
