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
import com.binomiaux.archimedes.repository.entities.StudentEnrollmentEntity;
import com.binomiaux.archimedes.repository.entities.StudentEntity;
import com.binomiaux.archimedes.repository.entities.TeacherEntity;

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
    public void create(Period period) {
        // Get teacher entity
        DynamoDbTable<TeacherEntity> teacherTable = enhancedClient.table(tableName, TeacherEntity.TABLE_SCHEMA);
        TeacherEntity teacherEntity = teacherTable.getItem(r -> r.key(k -> k
            .partitionValue("SCHOOL#" + period.getSchoolId())
            .sortValue("TEACHER#" + period.getTeacherId())
        ));

        if (teacherEntity == null) {
            throw new IllegalArgumentException("Teacher not found");
        }

        period.setPeriodId(period.getTeacherId() + "-" + period.getPeriodId());

        PeriodEntity periodEntity = new PeriodEntity();
        periodEntity.setPk("TEACHER#" + period.getTeacherId());
        periodEntity.setSk("PERIOD#" + period.getPeriodId());
        periodEntity.setType("PERIOD");
        periodEntity.setSchoolId(period.getSchoolId());
        periodEntity.setTeacherId(period.getTeacherId());
        periodEntity.setPeriodId(period.getPeriodId());
        periodEntity.setName(period.getName());
        periodEntity.setTeacherFirstName(teacherEntity.getFirstName());
        periodEntity.setTeacherLastName(teacherEntity.getLastName());
        periodEntity.setGsi1pk("PERIOD#" + period.getPeriodId());
        periodEntity.setGsi1sk("PERIOD#" + period.getPeriodId());
        periodEntity.setGsi2pk("PERIOD#" + period.getPeriodId());
        periodEntity.setGsi2sk("TEACHER#" + period.getTeacherId());

        DynamoDbTable<PeriodEntity> periodTable = enhancedClient.table(tableName, PeriodEntity.TABLE_SCHEMA);
        periodTable.putItem(periodEntity);
    }

    @Override
    public List<Period> getPeriodsByStudentId(String studentId) {
        DynamoDbTable<StudentEnrollmentEntity> studentEnrollmentTable = enhancedClient.table(tableName, StudentEnrollmentEntity.TABLE_SCHEMA);

        // LOOK UP BY GS1PK, AND GS1SK
        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue("STUDENT#" + studentId).sortValue("PERIOD#"));

        SdkIterable<Page<StudentEnrollmentEntity>> results = studentEnrollmentTable.index("gsi1")
            .query(r -> r.queryConditional(queryConditional));

        List<StudentEnrollmentEntity> results2 = results.stream()
            .map(Page::items)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        List<Period> periods = new ArrayList<>();
        for (StudentEnrollmentEntity entity : results2) {
                Period period = new Period();
                period.setPeriodId(entity.getPeriodId());
                period.setName(entity.getPeriodName());

                periods.add(period);
        }

        return periods;
    }

    @Override
    public List<Period> getPeriodsByTeacherId(String teacherId) {
        DynamoDbTable<PeriodEntity> studentEnrollmentTable = enhancedClient.table(tableName, PeriodEntity.TABLE_SCHEMA);

        // LOOK UP BY GS1PK, AND GS1SK
        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue("TEACHER#" + teacherId).sortValue("PERIOD#"));

        Iterable<Page<PeriodEntity>> results = studentEnrollmentTable.query(r -> r.queryConditional(queryConditional));

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
