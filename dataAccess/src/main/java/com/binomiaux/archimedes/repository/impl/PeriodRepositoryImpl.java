package com.binomiaux.archimedes.repository.impl;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.repository.api.PeriodRepository;
import com.binomiaux.archimedes.repository.entities.PeriodEntity;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@Repository
public class PeriodRepositoryImpl implements PeriodRepository {
    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    @Override
    public void create(Period period) {
        PeriodEntity periodEntity = new PeriodEntity();
        periodEntity.setPk("SCHOOL#" + period.getSchoolCode() + "#TEACHER#" + period.getTeacherCode() + "#PERIOD#" + period.getCode());
        periodEntity.setSk("#");
        periodEntity.setType("PERIOD");
        periodEntity.setCode(period.getCode());
        periodEntity.setSchoolCode(period.getSchoolCode());
        periodEntity.setTeacherCode(period.getTeacherCode());
        periodEntity.setName(period.getName());
        periodEntity.setAttendedBy(Collections.emptyList());

        DynamoDbTable<PeriodEntity> periodTable = enhancedClient.table(tableName, PeriodEntity.TABLE_SCHEMA);
        periodTable.putItem(periodEntity);
    }
}
