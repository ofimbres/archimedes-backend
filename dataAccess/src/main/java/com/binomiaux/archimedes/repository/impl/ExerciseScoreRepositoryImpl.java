package com.binomiaux.archimedes.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.model.ExerciseScore;
import com.binomiaux.archimedes.repository.api.ExerciseResultRepository;
import com.binomiaux.archimedes.repository.api.ExerciseScoreRepository;
import com.binomiaux.archimedes.repository.entities.ExerciseScoreEntity;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@Repository
public class ExerciseScoreRepositoryImpl implements ExerciseScoreRepository {
    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    @Override
    public void create(ExerciseScore score) {
        DynamoDbTable<ExerciseScoreEntity> exerciseScoreTable = enhancedClient.table(tableName, ExerciseScoreEntity.TABLE_SCHEMA);

        // get best record
        ExerciseScoreEntity exerciseScoreEntity = exerciseScoreTable.getItem(r -> r.key(Key.builder().partitionValue("PERIOD#" + score.getPeriod().getPeriodId() + "#STUDENT#" + score.getStudent().getStudentId()).sortValue("#SCORES#EXERCISE#" + score.getExercise().getExerciseId()).build()));
        if (exerciseScoreEntity == null) {
            //createExerciseScore(score, exerciseScoreTable, exerciseResultId);
            //ExerciseScoreEntity exerciseScoreRecord = new ExerciseScoreEntity();
            exerciseScoreEntity = new ExerciseScoreEntity();
            exerciseScoreEntity.setPk("PERIOD#" + score.getPeriod().getPeriodId() + "#STUDENT#" + score.getStudent().getSchoolId());
            exerciseScoreEntity.setSk("#SCORES#EXERCISE#" + score.getExercise().getExerciseId());
            exerciseScoreEntity.setGsi1pk("PERIOD#" + score.getPeriod().getPeriodId());
            exerciseScoreEntity.setGsi1sk("#SCORES#EXERCISE#" + score.getExercise().getExerciseId());
            exerciseScoreEntity.setType("EXERCISE_SCORE");
            exerciseScoreEntity.setExerciseId(exerciseResultId);
            exerciseScoreEntity.setStudentId(score.getStudent().getStudentId());
            exerciseScoreEntity.setPeriodId(score.getPeriod().getPeriodId());
            exerciseScoreEntity.setTries(1);
            exerciseScoreEntity.setBestScore(score.getScore());
            exerciseScoreEntity.setBestExerciseResult(exerciseResultId);
        } else {
            if (score.getScore() > exerciseScoreEntity.getBestScore()) {
                exerciseScoreEntity.setTries(exerciseScoreEntity.getTries() + 1);
                exerciseScoreEntity.setBestScore(score.getScore());
                exerciseScoreEntity.setBestExerciseResult(exerciseResultId);
            }
        }

        exerciseScoreTable.putItem(exerciseScoreEntity);
    }
}
