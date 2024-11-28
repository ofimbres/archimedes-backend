package com.binomiaux.archimedes.repository.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.repository.api.PeriodRepository;
import com.binomiaux.archimedes.repository.entities.PeriodEntity;
import com.binomiaux.archimedes.repository.entities.EnrollmentEntity;
import com.binomiaux.archimedes.repository.entities.TeacherEntity;
import com.binomiaux.archimedes.repository.exception.EntityNotFoundException;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class PeriodRepositoryImpl implements PeriodRepository {
    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    @Override
    public Period find(String periodId) {
        DynamoDbTable<PeriodEntity> periodTable = enhancedClient.table(tableName, PeriodEntity.TABLE_SCHEMA);

        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue("PERIOD#" + periodId).sortValue("#METADATA"));

        SdkIterable<Page<PeriodEntity>> results = periodTable//.index("gsi1")
            .query(r -> r.queryConditional(queryConditional));

        PeriodEntity periodEntity = results.stream()
            .map(x -> x.items())
            .flatMap(Collection::stream)
            .findFirst()
            .orElse(null);

        Period period = new Period();
        period.setPeriodId(periodEntity.getPeriodId());
        period.setName(periodEntity.getName());

        return period;
    }

    @Override
    public void create(Period period) {
        // Get teacher entity
        DynamoDbTable<TeacherEntity> teacherTable = enhancedClient.table(tableName, TeacherEntity.TABLE_SCHEMA);
        TeacherEntity teacherEntity = teacherTable.getItem(r -> r.key(k -> k
            .partitionValue("TEACHER#" + period.getTeacherId())
            .sortValue("#METADATA")
        ));

        if (teacherEntity == null) {
            throw new EntityNotFoundException("Teacher " + period.getTeacherId() + " not found", null);
        }

        period.setPeriodId(period.getTeacherId() + "-" + period.getPeriodId());

        PeriodEntity periodEntity = new PeriodEntity();
        periodEntity.setPk("PERIOD#" + period.getPeriodId());
        periodEntity.setSk("#METADATA");
        periodEntity.setType("PERIOD");
        periodEntity.setSchoolId(period.getSchoolId());
        periodEntity.setTeacherId(period.getTeacherId());
        periodEntity.setPeriodId(period.getPeriodId());
        periodEntity.setName(period.getName());
        periodEntity.setTeacherFirstName(teacherEntity.getFirstName());
        periodEntity.setTeacherLastName(teacherEntity.getLastName());
        periodEntity.setGsi1pk("TEACHER#" + period.getTeacherId());
        periodEntity.setGsi1sk("PERIOD#" + period.getPeriodId());
        periodEntity.setGsi2pk("PERIOD#" + period.getPeriodId());
        periodEntity.setGsi2sk("TEACHER#" + period.getTeacherId());

        DynamoDbTable<PeriodEntity> periodTable = enhancedClient.table(tableName, PeriodEntity.TABLE_SCHEMA);
        periodTable.putItem(periodEntity);
    }

    @Override
    public List<Period> getPeriodsByStudent(String studentId) {
        DynamoDbTable<EnrollmentEntity> studentEnrollmentTable = enhancedClient.table(tableName, EnrollmentEntity.TABLE_SCHEMA);

        // LOOK UP BY GS1PK, AND GS1SK
        QueryConditional queryConditional = QueryConditional.sortBeginsWith(k -> k.partitionValue("STUDENT#" + studentId).sortValue("PERIOD#"));

        SdkIterable<Page<EnrollmentEntity>> results = studentEnrollmentTable
            .query(r -> r.queryConditional(queryConditional));

        List<EnrollmentEntity> results2 = results.stream()
            .map(Page::items)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        List<Period> periods = new ArrayList<>();
        for (EnrollmentEntity entity : results2) {
                Period period = new Period();
                period.setPeriodId(entity.getPeriodId());
                period.setName(entity.getPeriodName());

                periods.add(period);
        }

        return periods;
    }

    @Override
    public List<Period> getPeriodsByTeacher(String teacherId) {
        DynamoDbTable<PeriodEntity> periodTable = enhancedClient.table(tableName, PeriodEntity.TABLE_SCHEMA);

        // LOOK UP BY GS1PK, AND GS1SK
        QueryConditional queryConditional = QueryConditional.sortBeginsWith(k -> k.partitionValue("TEACHER#" + teacherId).sortValue("PERIOD#"));

        SdkIterable<Page<PeriodEntity>> results = periodTable.index("gsi1").query(r -> r.queryConditional(queryConditional));

        List<Period> periods = new ArrayList<>();

        for (Page<PeriodEntity> page : results) {
            for (PeriodEntity entity : page.items()) {
                Period period = new Period();
                period.setPeriodId(entity.getPeriodId());
                period.setName(entity.getName());

                periods.add(period);
            }
        }

        return periods;
    }
}
