package com.binomiaux.archimedes.repository.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.repository.api.PeriodRepository;
import com.binomiaux.archimedes.repository.entities.PeriodEntity;
import com.binomiaux.archimedes.repository.entities.StudentEntity;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;

@Repository
public class PeriodRepositoryImpl implements PeriodRepository {
    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    @Override
    public void create(Period period) {
        PeriodEntity periodEntity = new PeriodEntity();
        periodEntity.setPk("PERIOD#" + period.getId());
        periodEntity.setSk("#");
        periodEntity.setType("PERIOD");
        periodEntity.setId(period.getId());
        periodEntity.setSchoolCode(period.getSchoolCode());
        periodEntity.setTeacherCode(period.getTeacherCode());
        periodEntity.setPeriodCode(period.getPeriodCode());
        periodEntity.setName(period.getName());
        periodEntity.setAttendedBy(Collections.emptyList());

        DynamoDbTable<PeriodEntity> periodTable = enhancedClient.table(tableName, PeriodEntity.TABLE_SCHEMA);
        periodTable.putItem(periodEntity);
    }

    @Override
    public List<Period> getPeriodsByStudentId(String studentId) {
        DynamoDbTable<StudentEntity> studentTable = enhancedClient.table(tableName, StudentEntity.TABLE_SCHEMA);

        StudentEntity studentEntity = studentTable.getItem(r -> r.key(k -> k.partitionValue("STUDENT#" + studentId).sortValue("#")));

        if (studentEntity == null) {
            return Collections.emptyList();
        }

        DynamoDbTable<PeriodEntity> periodTable = enhancedClient.table(tableName, PeriodEntity.TABLE_SCHEMA);

        List<String> periodIds = studentEntity.getAttends();

        if (periodIds.isEmpty()) {
            return Collections.emptyList();
        }

        // prepare list of keys
        List<Key> keys = periodIds.stream().map(id -> Key.builder().partitionValue("PERIOD#" + id).sortValue("#").build()).collect(Collectors.toList());

        ReadBatch.Builder<PeriodEntity> readBatchBuilder = ReadBatch.builder(PeriodEntity.class)
            .mappedTableResource(periodTable);

        keys.forEach(readBatchBuilder::addGetItem);
        ReadBatch readBatch = readBatchBuilder.build();

        BatchGetResultPageIterable batchResults = enhancedClient.batchGetItem(r -> r.addReadBatch(readBatch));
        List<PeriodEntity> periodEntities = batchResults.resultsForTable(periodTable).stream().collect(Collectors.toList());

        List<Period> periods = new ArrayList<>();
        for (PeriodEntity periodEntity : periodEntities) {
            Period period = new Period();
            period.setSchoolCode(periodEntity.getSchoolCode());
            period.setTeacherCode(periodEntity.getTeacherCode());
            period.setPeriodCode(periodEntity.getPeriodCode());
            period.setName(periodEntity.getName());
            periods.add(period);
        }

        return periods;
    }
}
