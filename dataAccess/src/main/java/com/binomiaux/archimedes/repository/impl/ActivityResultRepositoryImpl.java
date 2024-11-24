package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.api.ActivityResultRepository;
import com.binomiaux.archimedes.repository.entities.ActivityResultEntity;
import com.binomiaux.archimedes.repository.entities.ActivityScoreEntity;
import com.binomiaux.archimedes.repository.mapper.ActivityMapper;
import com.binomiaux.archimedes.repository.mapper.ActivityResultMapper;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import com.binomiaux.archimedes.model.Activity;
import com.binomiaux.archimedes.model.ActivityResult;
import com.binomiaux.archimedes.model.ActivityScore;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class ActivityResultRepositoryImpl implements ActivityResultRepository {

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    private ActivityResultMapper mapper = ActivityResultMapper.INSTANCE;

    @Override
    public ActivityResult find(String id) {
        DynamoDbTable<ActivityResultEntity> exerciseResultTable = enhancedClient.table(tableName, ActivityResultEntity.TABLE_SCHEMA);
        ActivityResultEntity entity = exerciseResultTable.getItem(r -> r.key(Key.builder().partitionValue("EXERCISE_RESULT#" + id).sortValue("#METADATA").build()));

        return mapper.entityToActivityResult(entity);
    }

    @Override
    public void create(ActivityResult result) {
        // exercise score
        DynamoDbTable<ActivityResultEntity> exerciseResultTable = enhancedClient.table(tableName, ActivityResultEntity.TABLE_SCHEMA);
        ActivityResultEntity record = new ActivityResultEntity();
        String exerciseResultId = UUID.randomUUID().toString();
        record.setPk("EXERCISE_RESULT#" + exerciseResultId);
        record.setSk("#METADATA");
        record.setGsi1pk("PERIOD#" + result.getPeriod().getPeriodId() + "#STUDENT#" + result.getStudent().getStudentId());
        
        LocalDate localDate = result.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = localDate.format(formatter);
        record.setGsi1sk("DATE#" + formattedDate);

        record.setGsi2pk("PERIOD#" + result.getPeriod().getPeriodId() +  "#STUDENT#" + result.getStudent().getStudentId());
        record.setGsi2sk("EXERCISE#" + result.getActivity().getActivityId());

        record.setType("EXERCISE_RESULT");

        record.setExerciseResultId(exerciseResultId);
        
        record.setPath(result.getResourcePath()); 
        record.setScore(result.getScore());
        record.setTimestamp(result.getTimestamp().toString());
        record.setExerciseId(result.getActivity().getActivityId());

        exerciseResultTable.putItem(record);
    }

    // @Override
    // public ActivityScore findByStudentIdAndExerciseId(String periodId, String studentId, String exerciseId) {
    //     DynamoDbTable<ExerciseScoreEntity> exerciseScoreTable = enhancedClient.table(tableName, ExerciseScoreEntity.TABLE_SCHEMA);
    //     ExerciseScoreEntity entity = exerciseScoreTable.getItem(r -> r.key(Key.builder().partitionValue("PERIOD#" + periodId + "#STUDENT#" + studentId).sortValue("#SCORES#EXERCISE#" + exerciseId).build()));

    //     // move to a transformer?
    //     return transform(entity);
    // }

    // @Override
    // public List<ActivityScore> findAllByClassIdAndExerciseId(String periodId, String exerciseId) {
    //     DynamoDbTable<ExerciseScoreEntity> exerciseScoreTable = enhancedClient.table(tableName, ExerciseScoreEntity.TABLE_SCHEMA);
    //     DynamoDbIndex<ExerciseScoreEntity> index = exerciseScoreTable.index("gsi1");
    //     Key key = Key.builder().partitionValue("PERIOD#" + periodId).sortValue("#SCORES#EXERCISE#" + exerciseId).build();
    //     QueryConditional queryConditional = QueryConditional.keyEqualTo(key);

    //     SdkIterable<Page<ExerciseScoreEntity>> pages = index.query(r -> r.queryConditional(queryConditional));
        
    //     return StreamSupport.stream(pages.spliterator(), false)
    //         .map(Page::items)
    //         .flatMap(List::stream)
    //         .map(this::transform)
    //         .collect(Collectors.toList());
    // }
}
