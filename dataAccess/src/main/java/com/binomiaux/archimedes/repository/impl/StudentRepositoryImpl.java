package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.api.StudentRepository;
import com.binomiaux.archimedes.repository.converter.StudentEntityTransform;
import com.binomiaux.archimedes.repository.entities.PeriodEntity;
import com.binomiaux.archimedes.repository.entities.SchoolEntity;
import com.binomiaux.archimedes.repository.entities.StudentEnrollmentEntity;
import com.binomiaux.archimedes.repository.entities.StudentEntity;
import com.binomiaux.archimedes.repository.exception.ConflictOperationException;
import com.binomiaux.archimedes.repository.exception.EntityNotFoundException;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;

import com.binomiaux.archimedes.model.Student;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepositoryImpl implements StudentRepository {

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    private StudentEntityTransform studentRecordTransform = new StudentEntityTransform();

    @Override
    public Student find(String studentId) {
        String pk = "STUDENT#" + studentId;
        DynamoDbTable<StudentEntity> studentTable = enhancedClient.table(tableName, StudentEntity.TABLE_SCHEMA);
        Key key = Key.builder().partitionValue(pk).sortValue("#METADATA").build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder().key(key).build();
        StudentEntity record = studentTable.getItem(request);

        return studentRecordTransform.transform(record);
    }

    @Override
    public void create(Student student) {
        // Update school entity
        DynamoDbTable<SchoolEntity> schoolTable = enhancedClient.table(tableName, SchoolEntity.TABLE_SCHEMA);
        SchoolEntity schoolEntity = schoolTable.getItem(r -> r.key(k -> k
            .partitionValue("SCHOOL#" + student.getSchoolId())
            .sortValue("#METADATA")
        ));
        
        // TODO Move to service layer
        if (schoolEntity == null) {
            throw new EntityNotFoundException("School " + student.getSchoolId() + " not found", null);
        }
        
        int nextStudentCode = schoolEntity.getStudentCount() + 1;
        schoolEntity.setStudentCount(nextStudentCode);
        student.setStudentId(student.getSchoolId() + "-S" + String.valueOf(nextStudentCode));

        // Update teacher entity
        StudentEntity studentEntity = new StudentEntity();
        studentEntity.setPk("STUDENT#" + student.getStudentId());
        studentEntity.setSk("#METADATA");
        studentEntity.setType("STUDENT");
        studentEntity.setSchoolId(student.getSchoolId());
        studentEntity.setStudentId(student.getStudentId());
        studentEntity.setFirstName(student.getFirstName());
        studentEntity.setLastName(student.getLastName());
        studentEntity.setEmail(student.getEmail());
        studentEntity.setUsername(student.getUsername());
        studentEntity.setGsi1pk("SCHOOL#" + student.getSchoolId());
        studentEntity.setGsi1sk("STUDENT#" + student.getStudentId());
        studentEntity.setGsi2pk("SCHOOL#" + student.getSchoolId());
        studentEntity.setGsi2sk("STUDENT#" + student.getFirstName() + " " + student.getLastName());

        DynamoDbTable<StudentEntity> studentTable = enhancedClient.table(tableName, StudentEntity.TABLE_SCHEMA);

        enhancedClient.transactWriteItems(b -> b
            .addPutItem(studentTable, TransactPutItemEnhancedRequest.builder(StudentEntity.class)
                .item(studentEntity)
                .conditionExpression(Expression.builder()
                    .expression("attribute_not_exists(pk)")
                    .build())
                .build())
            .addUpdateItem(schoolTable, schoolEntity)
        );
    }

    @Override
    public void enrollInPeriod(String studentId, String periodId) {
        DynamoDbTable<StudentEntity> studentTable = enhancedClient.table(tableName, StudentEntity.TABLE_SCHEMA);    
        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue("STUDENT#" + studentId).sortValue("#METADATA"));

        SdkIterable<Page<StudentEntity>> results = studentTable//.index("gsi1")
            .query(r -> r.queryConditional(queryConditional));

        StudentEntity studentEntity = results.stream()
            .map(x -> x.items())
            .flatMap(Collection::stream)
            .findFirst()
            .get();

        DynamoDbTable<PeriodEntity> periodTable = enhancedClient.table(tableName, PeriodEntity.TABLE_SCHEMA);
        QueryConditional queryConditional2 = QueryConditional.keyEqualTo(k -> k.partitionValue("PERIOD#" + periodId).sortValue("#METADATA"));

        SdkIterable<Page<PeriodEntity>> results2 = periodTable//.index("gsi1")
            .query(r -> r.queryConditional(queryConditional2));

        PeriodEntity periodEntity = results2.stream()
            .map(x -> x.items())
            .flatMap(Collection::stream)
            .findFirst()
            .get();

        DynamoDbTable<StudentEnrollmentEntity> studentEnrollmentTable = enhancedClient.table(tableName, StudentEnrollmentEntity.TABLE_SCHEMA);
        StudentEnrollmentEntity studentEnrollmentEntity = studentEnrollmentTable.getItem(r -> r.key(k -> k.partitionValue("PERIOD#" + periodId).sortValue("STUDENT#" + studentId)));
        if (studentEnrollmentEntity != null) {
            throw new ConflictOperationException("Student " + studentId + " already enrolled in period " + periodId, null, "STUDENT_ALREADY_ENROLLED");
        }

        studentEnrollmentEntity = new StudentEnrollmentEntity();
        studentEnrollmentEntity.setPk("STUDENT#" + studentId);
        studentEnrollmentEntity.setSk("PERIOD#" + periodId);
        studentEnrollmentEntity.setType("ENROLLMENT");
        studentEnrollmentEntity.setStudentId(studentId);
        studentEnrollmentEntity.setPeriodId(periodId);
        studentEnrollmentEntity.setStudentFirstName(studentEntity.getFirstName());
        studentEnrollmentEntity.setStudentLastName(studentEntity.getLastName());
        studentEnrollmentEntity.setPeriodName(periodEntity.getName());
        studentEnrollmentEntity.setGsi1pk("PERIOD#" + periodId);
        studentEnrollmentEntity.setGsi1sk("STUDENT#" + studentId);

        studentEnrollmentTable.putItem(studentEnrollmentEntity);
    }

    @Override
    public List<Student> getStudentsByPeriod(String periodId) {
        // PERIOD#1234-T1-6,	ENROLLMENT#
        DynamoDbTable<StudentEnrollmentEntity> studentEnrollmentTable = enhancedClient.table(tableName, StudentEnrollmentEntity.TABLE_SCHEMA);
        QueryConditional queryConditional = QueryConditional.sortBeginsWith(k -> k.partitionValue("PERIOD#" + periodId).sortValue("STUDENT#"));

        SdkIterable<Page<StudentEnrollmentEntity>> results = studentEnrollmentTable.index("gsi1")
            .query(r -> r.queryConditional(queryConditional));

        List<StudentEnrollmentEntity> studentEnrollmentEntityList = results.stream()
            .map(x -> x.items())
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        List<Student> students = new ArrayList<>();
        for (StudentEnrollmentEntity studentEnrollmentEntity : studentEnrollmentEntityList) {
            Student student = new Student();
            student.setStudentId(studentEnrollmentEntity.getStudentId());
            student.setFirstName(studentEnrollmentEntity.getStudentFirstName());
            student.setLastName(studentEnrollmentEntity.getStudentLastName());
            //student.setEmail(studentEnrollmentEntity.getStudentEmail());
            //student.setUsername(studentEnrollmentEntity.getStudentUsername());
            students.add(student);
        }

        return students;
    }
}
