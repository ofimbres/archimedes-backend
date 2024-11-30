package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.api.ActivityResultRepository;
import com.binomiaux.archimedes.repository.entities.ActivityResultEntity;
import com.binomiaux.archimedes.repository.mapper.ActivityResultMapper;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import com.binomiaux.archimedes.model.ActivityResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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
        ActivityResultEntity entity = exerciseResultTable.getItem(r -> r.key(Key.builder().partitionValue("ACTIVITY_RESULT#" + id).sortValue("#METADATA").build()));

        return mapper.entityToActivityResult(entity);
    }

    @Override
    public void create(ActivityResult result) {
        // exercise score
        DynamoDbTable<ActivityResultEntity> exerciseResultTable = enhancedClient.table(tableName, ActivityResultEntity.TABLE_SCHEMA);
        ActivityResultEntity record = new ActivityResultEntity();
        String exerciseResultId = result.getActivityResultId();

        record.setPk("ACTIVITY_RESULT#" + exerciseResultId);
        record.setSk("#METADATA");
        record.setGsi1pk("PERIOD#" + result.getPeriod().getPeriodId() + "#STUDENT#" + result.getStudent().getStudentId());
        
        LocalDate localDate = result.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = localDate.format(formatter);
        record.setGsi1sk("DATE#" + formattedDate);

        record.setGsi2pk("PERIOD#" + result.getPeriod().getPeriodId() +  "#STUDENT#" + result.getStudent().getStudentId());
        record.setGsi2sk("ACTIVITY#" + result.getActivity().getActivityId());

        record.setType("ACTIVITY_RESULT");

        record.setActivityResultId(exerciseResultId);
        
        record.setPath(result.getResourcePath()); 
        record.setScore(result.getScore());
        record.setTimestamp(result.getTimestamp().toString());
        record.setActivityId(result.getActivity().getActivityId());

        exerciseResultTable.putItem(record);
    }
}
