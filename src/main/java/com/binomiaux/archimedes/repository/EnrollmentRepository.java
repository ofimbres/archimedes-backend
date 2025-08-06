package com.binomiaux.archimedes.repository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.model.Enrollment;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.config.aws.DynamoDbProperties;
import com.binomiaux.archimedes.model.Enrollment;
import com.binomiaux.archimedes.exception.common.ConflictOperationException;
import com.binomiaux.archimedes.repository.util.DynamoKeyBuilder;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.Key;

/**
 * Simplified EnrollmentRepository without interface abstraction.
 * Uses DynamoKeyBuilder for consistent key generation and configuration injection.
 */
@Repository
public class EnrollmentRepository {
    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Autowired
    private DynamoDbProperties dynamoDbProperties;

    public void create(Enrollment enrollment) {
        Period period = enrollment.getPeriod();
        Student student = enrollment.getStudent();

        DynamoDbTable<Enrollment> studentEnrollmentTable = enhancedClient.table(dynamoDbProperties.getTableName(), Enrollment.TABLE_SCHEMA);
        Enrollment studentEnrollment = studentEnrollmentTable.getItem(r -> r.key(k -> k.partitionValue(DynamoKeyBuilder.buildStudentKey(student.getStudentId())).sortValue(DynamoKeyBuilder.buildPeriodKey(period.getPeriodId()))));
        if (studentEnrollment != null) {
            throw new ConflictOperationException("Student " + student.getStudentId() + " already enrolled in period " + period.getPeriodId(), null, "STUDENT_ALREADY_ENROLLED");
        }

        studentEnrollment = new Enrollment();
        studentEnrollment.setPk(DynamoKeyBuilder.buildStudentKey(student.getStudentId()));
        studentEnrollment.setSk(DynamoKeyBuilder.buildPeriodKey(period.getPeriodId()));
        studentEnrollment.setType("ENROLLMENT");
        studentEnrollment.setStudentId(student.getStudentId());
        studentEnrollment.setPeriodId(period.getPeriodId());
        studentEnrollment.setStudentFirstName(student.getFirstName());
        studentEnrollment.setStudentLastName(student.getLastName());
        studentEnrollment.setPeriodName(period.getName());
        studentEnrollment.setGsi1pk(DynamoKeyBuilder.buildPeriodKey(period.getPeriodId()));
        studentEnrollment.setGsi1sk(DynamoKeyBuilder.buildStudentKey(student.getStudentId()));

        studentEnrollmentTable.putItem(studentEnrollment);
    }

    public void delete(String studentId, String periodId) {
        DynamoDbTable<Enrollment> studentEnrollmentTable = enhancedClient.table(dynamoDbProperties.getTableName(), Enrollment.TABLE_SCHEMA);
        
        // Create the key to identify the item to be deleted
        Key key = Key.builder()
            .partitionValue(DynamoKeyBuilder.buildStudentKey(studentId))
            .sortValue(DynamoKeyBuilder.buildPeriodKey(periodId)) 
            .build();
            
        // Perform the delete operation
        studentEnrollmentTable.deleteItem(key);
    }

    public List<Enrollment> getEnrollmentsByPeriod(String periodId) {
        DynamoDbTable<Enrollment> studentEnrollmentTable = enhancedClient.table(dynamoDbProperties.getTableName(), Enrollment.TABLE_SCHEMA);

        // from gs1 

        QueryConditional queryConditional = QueryConditional.sortBeginsWith(k -> k.partitionValue(DynamoKeyBuilder.buildPeriodKey(periodId)).sortValue("STUDENT#"));

        SdkIterable<Page<Enrollment>> results = studentEnrollmentTable.index("gsi1").query(r -> r.queryConditional(queryConditional));

        List<Enrollment> enrollments = results.stream()
            .map(x -> x.items())
            .flatMap(Collection::stream)
            .map(e -> {
                Enrollment enrollment = new Enrollment();
                // Extract schoolId from studentId (assuming format: schoolId-S123)
                String schoolId = e.getStudentId().split("-")[0];
                // Extract teacherId from periodId (assuming format: teacherId-periodName)
                String teacherId = e.getPeriodId().split("-")[0];
                
                enrollment.setPeriod(new Period(schoolId, teacherId, e.getPeriodId(), e.getPeriodName()));
                enrollment.setStudent(new Student(schoolId, e.getStudentId(), e.getStudentFirstName(), e.getStudentLastName(), null, null));
                return enrollment;
            })
            .collect(Collectors.toList());

        return enrollments;
    }

}
