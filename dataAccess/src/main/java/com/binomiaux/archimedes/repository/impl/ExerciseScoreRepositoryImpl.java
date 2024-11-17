package com.binomiaux.archimedes.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.model.Exercise;
import com.binomiaux.archimedes.model.ExerciseScore;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.api.ExerciseScoreRepository;
import com.binomiaux.archimedes.repository.entities.ExerciseScoreEntity;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@Repository
public class ExerciseScoreRepositoryImpl implements ExerciseScoreRepository {
    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    @Override
    public void create(ExerciseScore score) {
        DynamoDbTable<ExerciseScoreEntity> exerciseScoreTable = enhancedClient.table(tableName, ExerciseScoreEntity.TABLE_SCHEMA);

        ExerciseScoreEntity exerciseScoreEntity = new ExerciseScoreEntity();
        exerciseScoreEntity.setPk("PERIOD#" + score.getPeriod().getPeriodId() + "#STUDENT#" + score.getStudent().getSchoolId());
        exerciseScoreEntity.setSk("#SCORES#EXERCISE#" + score.getExercise().getExerciseId());
        exerciseScoreEntity.setGsi1pk("PERIOD#" + score.getPeriod().getPeriodId());
        exerciseScoreEntity.setGsi1sk("#SCORES#EXERCISE#" + score.getExercise().getExerciseId());
        exerciseScoreEntity.setType("EXERCISE_SCORE");
        exerciseScoreEntity.setExerciseId(score.getExercise().getExerciseId());
        exerciseScoreEntity.setStudentId(score.getStudent().getStudentId());
        exerciseScoreEntity.setPeriodId(score.getPeriod().getPeriodId());
        exerciseScoreEntity.setTries(score.getTries());
        exerciseScoreEntity.setScore(score.getScore());
        exerciseScoreEntity.setExerciseResult(score.getExercise().getExerciseId());

        exerciseScoreTable.putItem(exerciseScoreEntity);
    }

    @Override
    public ExerciseScore find(String periodId, String studentId, String exerciseId) {
        DynamoDbTable<ExerciseScoreEntity> exerciseScoreTable = enhancedClient.table(tableName, ExerciseScoreEntity.TABLE_SCHEMA);

        ExerciseScoreEntity exerciseScoreEntity = exerciseScoreTable.getItem(r -> r.key(k -> k.partitionValue("PERIOD#" + periodId + "#STUDENT#" + studentId).sortValue("#SCORES#EXERCISE#" + exerciseId)));

        if (exerciseScoreEntity == null) {
            return null;
        }

        Exercise exercise = new Exercise();
        exercise.setExerciseId(exerciseScoreEntity.getExerciseId());

        Student student = new Student();
        student.setStudentId(exerciseScoreEntity.getStudentId());

        Period period = new Period();
        period.setPeriodId(exerciseScoreEntity.getPeriodId());

        ExerciseScore exerciseScore = new ExerciseScore();
        exerciseScore.setPeriod(period);
        exerciseScore.setStudent(student);
        exerciseScore.setExercise(exercise);
        exerciseScore.setTries(exerciseScoreEntity.getTries());
        exerciseScore.setScore(exerciseScoreEntity.getScore());
        exerciseScore.setExerciseResult(exerciseScoreEntity.getExerciseResult());

        return exerciseScore;
    }
}
