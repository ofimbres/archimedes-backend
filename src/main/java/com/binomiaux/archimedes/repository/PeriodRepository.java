package com.binomiaux.archimedes.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.config.aws.DynamoDbProperties;
import com.binomiaux.archimedes.exception.common.EntityNotFoundException;
import com.binomiaux.archimedes.model.Enrollment;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Teacher;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

/**
 * Simplified PeriodRepository without interface abstraction.
 * Direct implementation reduces complexity for single-implementation repositories.
 */
@Repository
public class PeriodRepository {
    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Autowired
    private DynamoDbProperties dynamoDbProperties;

    public Period find(String periodId) {
        DynamoDbTable<Period> periodTable = enhancedClient.table(dynamoDbProperties.getTableName(), Period.TABLE_SCHEMA);

        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue("PERIOD#" + periodId).sortValue("#METADATA"));

        SdkIterable<Page<Period>> results = periodTable
            .query(r -> r.queryConditional(queryConditional));

        Period periodEntity = results.stream()
            .map(x -> x.items())
            .flatMap(Collection::stream)
            .findFirst()
            .orElse(null);

        if (periodEntity == null) {
            return null;
        }

        // Return the entity directly as it already has the proper structure
        return periodEntity;
    }

    public void create(Period period) {
        // Get teacher entity to populate denormalized fields
        DynamoDbTable<Teacher> teacherTable = enhancedClient.table(dynamoDbProperties.getTableName(), Teacher.TABLE_SCHEMA);
        Teacher teacherEntity = teacherTable.getItem(r -> r.key(k -> k
            .partitionValue("TEACHER#" + period.getTeacherId())
            .sortValue("#METADATA")
        ));

        if (teacherEntity == null) {
            throw new EntityNotFoundException("Teacher " + period.getTeacherId() + " not found", null);
        }

        // Set denormalized teacher data
        period.setTeacherFirstName(teacherEntity.getFirstName());
        period.setTeacherLastName(teacherEntity.getLastName());
        period.setTeacherFullName("Ms. " + teacherEntity.getFirstName() + " " + teacherEntity.getLastName());
        
        // Generate all DynamoDB keys and metadata
        period.generateKeys();

        DynamoDbTable<Period> periodTable = enhancedClient.table(dynamoDbProperties.getTableName(), Period.TABLE_SCHEMA);
        periodTable.putItem(period);
    }

    public List<Period> getPeriodsByStudent(String studentId) {
        DynamoDbTable<Enrollment> enrollmentTable = enhancedClient.table(dynamoDbProperties.getTableName(), Enrollment.TABLE_SCHEMA);

        // Query GSI1 where parentEntityKey = STUDENT#studentId
        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue("STUDENT#" + studentId));

        SdkIterable<Page<Enrollment>> results = enrollmentTable.index("gsi1")
            .query(r -> r.queryConditional(queryConditional));

        List<Enrollment> enrollments = results.stream()
            .map(Page::items)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        List<Period> periods = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            // Create Period object from denormalized data in enrollment
            Period period = new Period();
            period.setPeriodId(enrollment.getPeriodId());
            period.setName(enrollment.getPeriodName());
            period.setPeriodNumber(Integer.valueOf(enrollment.getPeriodNumber()));
            
            periods.add(period);
        }

        return periods;
    }

    public List<Period> getPeriodsByTeacher(String teacherId) {
        DynamoDbTable<Period> periodTable = enhancedClient.table(dynamoDbProperties.getTableName(), Period.TABLE_SCHEMA);

        // Query GSI1 where parentEntityKey = TEACHER#teacherId
        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue("TEACHER#" + teacherId));

        SdkIterable<Page<Period>> results = periodTable.index("gsi1").query(r -> r.queryConditional(queryConditional));

        List<Period> periods = new ArrayList<>();

        for (Page<Period> page : results) {
            for (Period period : page.items()) {
                // Return the Period directly as it already has the proper structure
                periods.add(period);
            }
        }

        return periods;
    }
}
