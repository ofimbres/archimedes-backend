package com.binomiaux.archimedes.repository.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.model.Activity;
import com.binomiaux.archimedes.model.ActivityScore;
import com.binomiaux.archimedes.model.Enrollment;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.api.ActivityScoreRepository;
import com.binomiaux.archimedes.repository.entities.ActivityScoreEntity;
import com.binomiaux.archimedes.repository.mapper.ActivityScoreMapper;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class ActivityScoreRepositoryImpl implements ActivityScoreRepository {
    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    private ActivityScoreMapper mapper = ActivityScoreMapper.INSTANCE;

    @Override
    public ActivityScore findByPeriodAndStudentAndActivity(String periodId, String studentId, String exerciseId) {
        DynamoDbTable<ActivityScoreEntity> exerciseScoreTable = enhancedClient.table(tableName, ActivityScoreEntity.TABLE_SCHEMA);

        ActivityScoreEntity exerciseScoreEntity = exerciseScoreTable.getItem(r -> r.key(k -> k.partitionValue("PERIOD#" + periodId + "#STUDENT#" + studentId).sortValue("#SCORES#ACTIVITY#" + exerciseId)));

        if (exerciseScoreEntity == null) {
            return null;
        }

        return mapper.entityToActivityResult(exerciseScoreEntity);
    }

    @Override
    public List<ActivityScore> findByPeriodAndActivity(String periodId, String activityId) {
        DynamoDbTable<ActivityScoreEntity> exerciseScoreTable = enhancedClient.table(tableName, ActivityScoreEntity.TABLE_SCHEMA);

        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue("PERIOD#" + periodId).sortValue("#SCORES#ACTIVITY#" + activityId));
        SdkIterable<Page<ActivityScoreEntity>> results = exerciseScoreTable.index("gsi1").query(r -> r.queryConditional(queryConditional));

        List<ActivityScore> scores = results.stream()
            .map(x -> x.items())
            .flatMap(Collection::stream)
            .map(e -> {
                return mapper.entityToActivityResult(e);
            })
            .collect(Collectors.toList());

        return scores;
    }

    @Override
    public List<ActivityScore> findByPeriodAndStudent(String periodId, String studentId) {
        DynamoDbTable<ActivityScoreEntity> exerciseScoreTable = enhancedClient.table(tableName, ActivityScoreEntity.TABLE_SCHEMA);

        QueryConditional queryConditional = QueryConditional.sortBeginsWith(k -> k.partitionValue("PERIOD#" + periodId + "#STUDENT#" + studentId).sortValue("#SCORES#"));
        SdkIterable<Page<ActivityScoreEntity>> results = exerciseScoreTable.query(r -> r.queryConditional(queryConditional));
    
        List<ActivityScore> scores = results.stream()
            .map(Page::items)
            .flatMap(Collection::stream)
            .map(mapper::entityToActivityResult)
            .collect(Collectors.toList());
    
        return scores;
    }

    @Override
    public List<ActivityScore> findByPeriod(String periodId) {
        DynamoDbTable<ActivityScoreEntity> exerciseScoreTable = enhancedClient.table(tableName, ActivityScoreEntity.TABLE_SCHEMA);

        QueryConditional queryConditional = QueryConditional.sortBeginsWith(k -> k.partitionValue("PERIOD#" + periodId).sortValue("#SCORES#"));
        SdkIterable<Page<ActivityScoreEntity>> results = exerciseScoreTable.index("gsi1").query(r -> r.queryConditional(queryConditional));
    
        List<ActivityScore> scores = results.stream()
            .map(Page::items)
            .flatMap(Collection::stream)
            .map(mapper::entityToActivityResult)
            .collect(Collectors.toList());
    
        return scores;
    }

    @Override
    public void create(ActivityScore score) {
        DynamoDbTable<ActivityScoreEntity> exerciseScoreTable = enhancedClient.table(tableName, ActivityScoreEntity.TABLE_SCHEMA);

        ActivityScoreEntity exerciseScoreEntity = new ActivityScoreEntity();
        exerciseScoreEntity.setPk("PERIOD#" + score.getPeriod().getPeriodId() + "#STUDENT#" + score.getStudent().getStudentId());
        exerciseScoreEntity.setSk("#SCORES#ACTIVITY#" + score.getActivity().getActivityId());
        exerciseScoreEntity.setGsi1pk("PERIOD#" + score.getPeriod().getPeriodId());
        exerciseScoreEntity.setGsi1sk("#SCORES#ACTIVITY#" + score.getActivity().getActivityId());
        exerciseScoreEntity.setType("ACTIVITY_SCORE");
        exerciseScoreEntity.setActivityId(score.getActivity().getActivityId());
        exerciseScoreEntity.setStudentId(score.getStudent().getStudentId());
        exerciseScoreEntity.setStudentFirstName(score.getStudent().getFirstName());
        exerciseScoreEntity.setStudentLastName(score.getStudent().getLastName());
        exerciseScoreEntity.setPeriodId(score.getPeriod().getPeriodId());
        exerciseScoreEntity.setTries(score.getTries());
        exerciseScoreEntity.setScore(score.getScore());
        exerciseScoreEntity.setActivityResultId(score.getActivityResult().getActivityResultId());

        exerciseScoreTable.putItem(exerciseScoreEntity);
    }
}
