package com.binomiaux.archimedes.repository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.config.aws.DynamoDbProperties;
import com.binomiaux.archimedes.exception.common.ConflictOperationException;
import com.binomiaux.archimedes.model.Enrollment;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

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

    public void create(Student student, Period period) {
        DynamoDbTable<Enrollment> enrollmentTable = enhancedClient.table(dynamoDbProperties.getTableName(), Enrollment.TABLE_SCHEMA);
        
        // Check if enrollment already exists using new schema
        String enrollmentPk = "ENROLLMENT#" + generateEnrollmentId(student.getStudentId(), period.getPeriodId());
        Enrollment existingEnrollment = enrollmentTable.getItem(r -> r.key(k -> k.partitionValue(enrollmentPk).sortValue("#METADATA")));
        if (existingEnrollment != null) {
            throw new ConflictOperationException("Student " + student.getStudentId() + " already enrolled in period " + period.getPeriodId(), null, "STUDENT_ALREADY_ENROLLED");
        }

        // Create new enrollment with denormalized data
        Enrollment enrollment = new Enrollment(student.getStudentId(), period.getPeriodId());
        enrollment.setEnrollmentId(generateEnrollmentId(student.getStudentId(), period.getPeriodId()));
        enrollment.setStudentFullName(student.getFullName());
        enrollment.setStudentFirstName(student.getFirstName());
        enrollment.setStudentLastName(student.getLastName());
        enrollment.setPeriodDisplayName(period.getName() + " (Period " + period.getPeriodNumber() + ")");
        enrollment.setPeriodName(period.getName());
        enrollment.setPeriodNumber(String.valueOf(period.getPeriodNumber()));
        enrollment.setTeacherLastName(period.getTeacherLastName());
        enrollment.setEnrollmentDate(java.time.LocalDate.now().toString());
        enrollment.generateKeys();

        enrollmentTable.putItem(enrollment);
    }

    // Overloaded method for backward compatibility with Enrollment object
    public void create(Enrollment enrollment) {
        // This method assumes the enrollment already has all required fields set
        DynamoDbTable<Enrollment> enrollmentTable = enhancedClient.table(dynamoDbProperties.getTableName(), Enrollment.TABLE_SCHEMA);
        
        // Generate keys if not already set
        if (enrollment.getPk() == null) {
            enrollment.generateKeys();
        }
        
        enrollmentTable.putItem(enrollment);
    }

    public void delete(String studentId, String periodId) {
        DynamoDbTable<Enrollment> enrollmentTable = enhancedClient.table(dynamoDbProperties.getTableName(), Enrollment.TABLE_SCHEMA);
        
        // Create the key using new schema (ENROLLMENT#ID)
        String enrollmentId = generateEnrollmentId(studentId, periodId);
        Key key = Key.builder()
            .partitionValue("ENROLLMENT#" + enrollmentId)
            .sortValue("#METADATA") 
            .build();
            
        // Perform the delete operation
        enrollmentTable.deleteItem(key);
    }

    public List<Enrollment> getEnrollmentsByPeriod(String periodId) {
        DynamoDbTable<Enrollment> enrollmentTable = enhancedClient.table(dynamoDbProperties.getTableName(), Enrollment.TABLE_SCHEMA);

        // Query GSI2 to get all enrollments for a period using new schema
        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue("PERIOD#" + periodId));

        SdkIterable<Page<Enrollment>> results = enrollmentTable.index("gsi2").query(r -> r.queryConditional(queryConditional));

        List<Enrollment> enrollments = results.stream()
            .map(x -> x.items())
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        return enrollments;
    }

    public List<Enrollment> getEnrollmentsByStudent(String studentId) {
        DynamoDbTable<Enrollment> enrollmentTable = enhancedClient.table(dynamoDbProperties.getTableName(), Enrollment.TABLE_SCHEMA);

        // Query GSI1 to get all enrollments for a student using new schema
        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue("STUDENT#" + studentId));

        SdkIterable<Page<Enrollment>> results = enrollmentTable.index("gsi1").query(r -> r.queryConditional(queryConditional));

        List<Enrollment> enrollments = results.stream()
            .map(x -> x.items())
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        return enrollments;
    }

    // Helper method to generate consistent enrollment IDs
    private String generateEnrollmentId(String studentId, String periodId) {
        return "ENR_" + studentId + "_" + periodId;
    }

}
