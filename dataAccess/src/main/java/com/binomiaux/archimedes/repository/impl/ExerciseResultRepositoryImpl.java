package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.api.ExerciseResultRepository;
import com.binomiaux.archimedes.repository.entities.ExerciseResultEntity;
import com.binomiaux.archimedes.repository.entities.ExerciseScoreEntity;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import com.binomiaux.archimedes.model.Exercise;
import com.binomiaux.archimedes.model.ExerciseResult;
import com.binomiaux.archimedes.model.ExerciseScore;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class ExerciseResultRepositoryImpl implements ExerciseResultRepository {

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    @Override
    public void create(ExerciseResult result) {
        // exercise score
        DynamoDbTable<ExerciseResultEntity> exerciseResultTable = enhancedClient.table(tableName, ExerciseResultEntity.TABLE_SCHEMA);
        ExerciseResultEntity record = new ExerciseResultEntity();
        String exerciseResultId = UUID.randomUUID().toString();
        record.setPk("EXERCISE_RESULT#" + exerciseResultId);
        record.setSk("#METADATA");
        record.setGsi1pk("STUDENT#" + result.getStudent().getSchoolId());
        
        LocalDate localDate = result.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = localDate.format(formatter);
        record.setGsi1sk("DATE#" + formattedDate);

        record.setGsi2pk("STUDENT#" + result.getStudent().getSchoolId());
        record.setGsi2sk("EXERCISE#" + result.getExercise().getExerciseId());

        record.setType("EXERCISE_RESULT");

        record.setExerciseResultId(exerciseResultId);
        record.setPath(result.getS3Key());
        record.setScore(result.getScore());
        record.setTimestamp(result.getTimestamp().toString());
        record.setExerciseId(result.getExercise().getExerciseId());

        exerciseResultTable.putItem(record);
    }



    @Override
    public List<ExerciseResult> findByStudentId(String classId, String studentId, String exerciseId) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public ExerciseScore findByStudentIdAndExerciseId(String periodId, String studentId, String exerciseId) {
        DynamoDbTable<ExerciseScoreEntity> exerciseScoreTable = enhancedClient.table(tableName, ExerciseScoreEntity.TABLE_SCHEMA);
        ExerciseScoreEntity entity = exerciseScoreTable.getItem(r -> r.key(Key.builder().partitionValue("PERIOD#" + periodId + "#STUDENT#" + studentId).sortValue("#SCORES#EXERCISE#" + exerciseId).build()));

        // move to a transformer?
        return transform(entity);
    }

    @Override
    public List<ExerciseScore> findAllByClassIdAndExerciseId(String periodId, String exerciseId) {
        DynamoDbTable<ExerciseScoreEntity> exerciseScoreTable = enhancedClient.table(tableName, ExerciseScoreEntity.TABLE_SCHEMA);
        DynamoDbIndex<ExerciseScoreEntity> index = exerciseScoreTable.index("gsi1");
        Key key = Key.builder().partitionValue("PERIOD#" + periodId).sortValue("#SCORES#EXERCISE#" + exerciseId).build();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);

        SdkIterable<Page<ExerciseScoreEntity>> pages = index.query(r -> r.queryConditional(queryConditional));
        
        return StreamSupport.stream(pages.spliterator(), false)
            .map(Page::items)
            .flatMap(List::stream)
            .map(this::transform)
            .collect(Collectors.toList());
    }

    private ExerciseScore transform(ExerciseScoreEntity entity) {
        ExerciseScore exerciseScore = new ExerciseScore();
        Exercise exercise = new Exercise();
        exercise.setExerciseId(entity.getExerciseId());
        Student student = new Student();
        student.setStudentId(entity.getStudentId());
        Period period = new Period();
        period.setPeriodId(entity.getPeriodId());

        exerciseScore.setExercise(exercise);
        exerciseScore.setStudent(student);
        exerciseScore.setPeriod(period);
        exerciseScore.setTries(entity.getTries());
        exerciseScore.setScore(entity.getScore());
        exerciseScore.setExerciseResult(entity.getExerciseResult());

        return exerciseScore;
    }
}
