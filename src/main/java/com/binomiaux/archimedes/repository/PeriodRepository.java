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

    public Period find(String compositePeriodId) {
        // Parse school-scoped period ID: SCH001#P001
        String[] parts = compositePeriodId.split("#");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid period ID format. Expected: SCH001#P001, got: " + compositePeriodId);
        }
        
        String schoolId = parts[0];
        String periodId = parts[1];
        
        return find(schoolId, periodId);
    }

    public Period find(String schoolId, String periodId) {
        DynamoDbTable<Period> periodTable = enhancedClient.table(dynamoDbProperties.getTableName(), Period.TABLE_SCHEMA);

        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k
            .partitionValue(Period.buildPartitionKey(schoolId, periodId))
            .sortValue(Period.buildSortKey()));

        SdkIterable<Page<Period>> results = periodTable
            .query(r -> r.queryConditional(queryConditional));

        Period periodEntity = results.stream()
            .map(x -> x.items())
            .flatMap(Collection::stream)
            .findFirst()
            .orElse(null);

        return periodEntity;
    }

    public void create(Period period) {
        // Get teacher entity to populate denormalized fields
        DynamoDbTable<Teacher> teacherTable = enhancedClient.table(dynamoDbProperties.getTableName(), Teacher.TABLE_SCHEMA);
        Teacher teacherEntity = teacherTable.getItem(r -> r.key(k -> k
            .partitionValue(Teacher.buildPartitionKey(period.getSchoolId(), period.getTeacherId()))
            .sortValue(Teacher.buildSortKey())
        ));

        if (teacherEntity == null) {
            throw new EntityNotFoundException("Teacher " + period.getTeacherId() + " not found", null);
        }

        // Set denormalized teacher data
        period.setTeacherFirstName(teacherEntity.getFirstName());
        period.setTeacherLastName(teacherEntity.getLastName());
        period.setTeacherFullName(teacherEntity.getFirstName() + " " + teacherEntity.getLastName());
        
        // Generate all DynamoDB keys and metadata
        period.generateKeys();

        DynamoDbTable<Period> periodTable = enhancedClient.table(dynamoDbProperties.getTableName(), Period.TABLE_SCHEMA);
        periodTable.putItem(period);
    }

    /**
     * Get all periods in a school - for school admin view
     * Uses scan with filter since periods are now individually partitioned
     */
    public List<Period> getPeriodsBySchool(String schoolId) {
        DynamoDbTable<Period> periodTable = enhancedClient.table(dynamoDbProperties.getTableName(), Period.TABLE_SCHEMA);

        // Scan with filter for periods in the school
        // TODO: Consider adding a GSI for school-based queries if this becomes a performance issue
        return periodTable.scan()
            .items()
            .stream()
            .filter(period -> schoolId.equals(period.getSchoolId()) && "PERIOD".equals(period.getEntityType()))
            .collect(Collectors.toList());
    }

    /**
     * Get periods by teacher within a specific school - maintains teacher-period relationship
     */
    public List<Period> getPeriodsByTeacherInSchool(String schoolId, String teacherId) {
        DynamoDbTable<Period> periodTable = enhancedClient.table(dynamoDbProperties.getTableName(), Period.TABLE_SCHEMA);

        // Query using teacher-periods GSI
        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k
            .partitionValue("TEACHER#" + schoolId + "#" + teacherId));

        SdkIterable<Page<Period>> results = periodTable
            .index("teacher-periods-index")
            .query(r -> r.queryConditional(queryConditional));

        return results.stream()
            .map(Page::items)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    /**
     * Get periods by student within a specific school - for student dashboard
     */
    public List<Period> getPeriodsByStudentInSchool(String schoolId, String studentId) {
        // Get enrollments for student, then fetch period details
        DynamoDbTable<Enrollment> enrollmentTable = enhancedClient.table(dynamoDbProperties.getTableName(), Enrollment.TABLE_SCHEMA);

        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue("STUDENT#" + schoolId + "#" + studentId));

        SdkIterable<Page<Enrollment>> results = enrollmentTable.index("gsi1")
            .query(r -> r.queryConditional(queryConditional));

        List<Enrollment> enrollments = results.stream()
            .map(Page::items)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        // Convert to periods (could be optimized with batch get if needed)
        List<Period> periods = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            Period period = find(schoolId, enrollment.getPeriodId());
            if (period != null) {
                periods.add(period);
            }
        }

        return periods;
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
        // Legacy method - assumes teacherId includes school context
        // TODO: Consider deprecating in favor of school-scoped method
        DynamoDbTable<Period> periodTable = enhancedClient.table(dynamoDbProperties.getTableName(), Period.TABLE_SCHEMA);

        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue("TEACHER#" + teacherId));

        SdkIterable<Page<Period>> results = periodTable.index("teacher-periods-index")
            .query(r -> r.queryConditional(queryConditional));

        return results.stream()
            .map(Page::items)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    /**
     * Find a period by school and period ID.
     * Used for registration codes where teacher ID is not available.
     * 
     * @param schoolId School identifier (e.g., "SCH001")
     * @param periodId Simple period identifier (e.g., "P001")
     * @return Period if found, null otherwise
     */
    public Period findPeriodInSchool(String schoolId, String periodId) {
        // With simplified structure, this is just a direct lookup
        return find(schoolId, periodId);
    }
}
