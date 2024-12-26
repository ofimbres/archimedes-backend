package com.binomiaux.archimedes.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.model.Enrollment;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.api.EnrollmentRepository;
import com.binomiaux.archimedes.repository.entities.EnrollmentEntity;
import com.binomiaux.archimedes.repository.entities.PeriodEntity;
import com.binomiaux.archimedes.repository.entities.StudentEntity;
import com.binomiaux.archimedes.repository.exception.ConflictOperationException;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Repository
public class EnrollmentRepositoryImpl implements EnrollmentRepository {
    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    @Override
    public void create(Enrollment enrollment) {
        Period period = enrollment.getPeriod();
        Student student = enrollment.getStudent();

        DynamoDbTable<EnrollmentEntity> studentEnrollmentTable = enhancedClient.table(tableName, EnrollmentEntity.TABLE_SCHEMA);
        EnrollmentEntity studentEnrollmentEntity = studentEnrollmentTable.getItem(r -> r.key(k -> k.partitionValue("PERIOD#" + period.getPeriodId()).sortValue("STUDENT#" + student.getStudentId())));
        if (studentEnrollmentEntity != null) {
            throw new ConflictOperationException("Student " + student.getStudentId() + " already enrolled in period " + period.getPeriodId(), null, "STUDENT_ALREADY_ENROLLED");
        }

        studentEnrollmentEntity = new EnrollmentEntity();
        studentEnrollmentEntity.setPk("STUDENT#" + student.getStudentId());
        studentEnrollmentEntity.setSk("PERIOD#" + period.getPeriodId());
        studentEnrollmentEntity.setType("ENROLLMENT");
        studentEnrollmentEntity.setStudentId(student.getStudentId());
        studentEnrollmentEntity.setPeriodId(period.getPeriodId());
        studentEnrollmentEntity.setStudentFirstName(student.getFirstName());
        studentEnrollmentEntity.setStudentLastName(student.getLastName());
        studentEnrollmentEntity.setPeriodName(period.getName());
        studentEnrollmentEntity.setGsi1pk("PERIOD#" + period.getPeriodId());
        studentEnrollmentEntity.setGsi1sk("STUDENT#" + student.getStudentId());

        studentEnrollmentTable.putItem(studentEnrollmentEntity);
    }

    @Override
    public void delete(String studentId, String periodId) {
        DynamoDbTable<EnrollmentEntity> studentEnrollmentTable = enhancedClient.table(tableName, EnrollmentEntity.TABLE_SCHEMA);
        
        // Create the key to identify the item to be deleted
        Key key = Key.builder()
            .partitionValue("STUDENT#" + studentId)
            .sortValue("PERIOD#" + periodId)
            .build();
            
        // Perform the delete operation
        studentEnrollmentTable.deleteItem(key);
    }

    @Override
    public List<Enrollment> getEnrollmentsByPeriod(String periodId) {
        DynamoDbTable<EnrollmentEntity> studentEnrollmentTable = enhancedClient.table(tableName, EnrollmentEntity.TABLE_SCHEMA);

        // from gs1 

        QueryConditional queryConditional = QueryConditional.sortBeginsWith(k -> k.partitionValue("PERIOD#" + periodId).sortValue("STUDENT#"));

        SdkIterable<Page<EnrollmentEntity>> results = studentEnrollmentTable.index("gsi1").query(r -> r.queryConditional(queryConditional));

        // TODO: Create a mapper
        List<Enrollment> enrollments = results.stream()
            .map(x -> x.items())
            .flatMap(Collection::stream)
            .map(e -> {
                Enrollment enrollment = new Enrollment();
                enrollment.setPeriod(new Period(null, null, e.getPeriodId(), e.getPeriodName()));
                enrollment.setStudent(new Student(null, e.getStudentId(), e.getStudentFirstName(), e.getStudentLastName(), null, null));
                return enrollment;
            })
            .collect(Collectors.toList());

        return enrollments;
    }

}
