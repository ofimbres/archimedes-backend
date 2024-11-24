package com.binomiaux.archimedes.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.model.Activity;
import com.binomiaux.archimedes.model.ActivityScore;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.api.ActivityScoreRepository;
import com.binomiaux.archimedes.repository.entities.ActivityScoreEntity;
import com.binomiaux.archimedes.repository.mapper.ActivityScoreMapper;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@Repository
public class ActivityScoreRepositoryImpl implements ActivityScoreRepository {
    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    private ActivityScoreMapper mapper = ActivityScoreMapper.INSTANCE;

    @Override
    public void create(ActivityScore score) {
        DynamoDbTable<ActivityScoreEntity> exerciseScoreTable = enhancedClient.table(tableName, ActivityScoreEntity.TABLE_SCHEMA);

        ActivityScoreEntity exerciseScoreEntity = new ActivityScoreEntity();
        exerciseScoreEntity.setPk("PERIOD#" + score.getPeriod().getPeriodId() + "#STUDENT#" + score.getStudent().getStudentId());
        exerciseScoreEntity.setSk("#SCORES#ACTIVITY#" + score.getActivity().getActivityId());
        exerciseScoreEntity.setGsi1pk("PERIOD#" + score.getPeriod().getPeriodId());
        exerciseScoreEntity.setGsi1sk("#SCORES#ACTIVITY#" + score.getActivity().getActivityId());
        exerciseScoreEntity.setType("EXERCISE_SCORE");
        exerciseScoreEntity.setExerciseId(score.getActivity().getActivityId());
        exerciseScoreEntity.setStudentId(score.getStudent().getStudentId());
        exerciseScoreEntity.setPeriodId(score.getPeriod().getPeriodId());
        exerciseScoreEntity.setTries(score.getTries());
        exerciseScoreEntity.setScore(score.getScore());
        exerciseScoreEntity.setExerciseResult(score.getActivity().getActivityId());

        exerciseScoreTable.putItem(exerciseScoreEntity);
    }

    @Override
    public ActivityScore find(String periodId, String studentId, String exerciseId) {
        DynamoDbTable<ActivityScoreEntity> exerciseScoreTable = enhancedClient.table(tableName, ActivityScoreEntity.TABLE_SCHEMA);

        ActivityScoreEntity exerciseScoreEntity = exerciseScoreTable.getItem(r -> r.key(k -> k.partitionValue("PERIOD#" + periodId + "#STUDENT#" + studentId).sortValue("#SCORES#ACTIVITY#" + exerciseId)));

        if (exerciseScoreEntity == null) {
            return null;
        }

        return mapper.entityToActivityResult(exerciseScoreEntity);
    }
}
