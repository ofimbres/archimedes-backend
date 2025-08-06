package com.binomiaux.archimedes.repository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.model.ActivityScore;
import com.binomiaux.archimedes.config.aws.DynamoDbProperties;
import com.binomiaux.archimedes.model.ActivityScore;
import com.binomiaux.archimedes.repository.util.DynamoKeyBuilder;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

/**
 * Simplified ActivityScoreRepository without interface abstraction.
 * Uses DynamoKeyBuilder for consistent key generation and configuration injection.
 */
@Repository
public class ActivityScoreRepository {
    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Autowired
    private DynamoDbProperties dynamoDbProperties;


    public ActivityScore findByPeriodAndStudentAndActivity(String periodId, String studentId, String exerciseId) {
        DynamoDbTable<ActivityScore> exerciseScoreTable = enhancedClient.table(dynamoDbProperties.getTableName(), ActivityScore.TABLE_SCHEMA);

        String pk = DynamoKeyBuilder.buildPeriodPK(periodId) + "#STUDENT#" + studentId;
        String sk = "#SCORES#ACTIVITY#" + exerciseId;
        ActivityScore exerciseScoreEntity = exerciseScoreTable.getItem(r -> r.key(k -> k.partitionValue(pk).sortValue(sk)));

        if (exerciseScoreEntity == null) {
            return null;
        }

        return exerciseScoreEntity;
    }

    public List<ActivityScore> findByPeriodAndActivity(String periodId, String activityId) {
        DynamoDbTable<ActivityScore> exerciseScoreTable = enhancedClient.table(dynamoDbProperties.getTableName(), ActivityScore.TABLE_SCHEMA);

        String pk = DynamoKeyBuilder.buildPeriodPK(periodId);
        String sk = "#SCORES#ACTIVITY#" + activityId;
        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue(pk).sortValue(sk));
        SdkIterable<Page<ActivityScore>> results = exerciseScoreTable.index(dynamoDbProperties.getGsi1Name()).query(r -> r.queryConditional(queryConditional));

        List<ActivityScore> scores = results.stream()
            .map(x -> x.items())
            .flatMap(Collection::stream)
            
            .collect(Collectors.toList());

        return scores;
    }

    public List<ActivityScore> findByPeriodAndStudent(String periodId, String studentId) {
        DynamoDbTable<ActivityScore> exerciseScoreTable = enhancedClient.table(dynamoDbProperties.getTableName(), ActivityScore.TABLE_SCHEMA);

        String pk = DynamoKeyBuilder.buildPeriodPK(periodId) + "#STUDENT#" + studentId;
        QueryConditional queryConditional = QueryConditional.sortBeginsWith(k -> k.partitionValue(pk).sortValue("#SCORES#"));
        SdkIterable<Page<ActivityScore>> results = exerciseScoreTable.query(r -> r.queryConditional(queryConditional));
    
        List<ActivityScore> scores = results.stream()
            .map(Page::items)
            .flatMap(Collection::stream)
            
            .collect(Collectors.toList());
    
        return scores;
    }

    public List<ActivityScore> findByPeriod(String periodId) {
        DynamoDbTable<ActivityScore> exerciseScoreTable = enhancedClient.table(dynamoDbProperties.getTableName(), ActivityScore.TABLE_SCHEMA);

        String pk = DynamoKeyBuilder.buildPeriodPK(periodId);
        QueryConditional queryConditional = QueryConditional.sortBeginsWith(k -> k.partitionValue(pk).sortValue("#SCORES#"));
        SdkIterable<Page<ActivityScore>> results = exerciseScoreTable.index(dynamoDbProperties.getGsi1Name()).query(r -> r.queryConditional(queryConditional));
    
        List<ActivityScore> scores = results.stream()
            .map(Page::items)
            .flatMap(Collection::stream)
            
            .collect(Collectors.toList());
    
        return scores;
    }

    public void create(ActivityScore score) {
        DynamoDbTable<ActivityScore> exerciseScoreTable = enhancedClient.table(dynamoDbProperties.getTableName(), ActivityScore.TABLE_SCHEMA);

        ActivityScore exerciseScoreEntity = new ActivityScore();
        String pk = DynamoKeyBuilder.buildPeriodPK(score.getPeriod().getPeriodId()) + "#STUDENT#" + score.getStudent().getStudentId();
        String sk = "#SCORES#ACTIVITY#" + score.getActivity().getActivityId();
        String gsi1pk = DynamoKeyBuilder.buildPeriodPK(score.getPeriod().getPeriodId());
        String gsi1sk = "#SCORES#ACTIVITY#" + score.getActivity().getActivityId();
        
        exerciseScoreEntity.setPk(pk);
        exerciseScoreEntity.setSk(sk);
        exerciseScoreEntity.setGsi1pk(gsi1pk);
        exerciseScoreEntity.setGsi1sk(gsi1sk);
        exerciseScoreEntity.setType("ACTIVITY_SCORE");
        exerciseScoreEntity.setActivityId(score.getActivity().getActivityId());
        exerciseScoreEntity.setStudentId(score.getStudent().getStudentId());
        exerciseScoreEntity.setStudentFirstName(score.getStudent().getFirstName());
        exerciseScoreEntity.setStudentLastName(score.getStudent().getLastName());
        exerciseScoreEntity.setPeriodId(score.getPeriod().getPeriodId());
        exerciseScoreEntity.setTries(score.getTries());
        exerciseScoreEntity.setScore(score.getScore());
        exerciseScoreEntity.setActivityResultId(score.getActivitySubmission().getActivityResultId());

        exerciseScoreTable.putItem(exerciseScoreEntity);
    }
}
