package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.api.StudentRepository;
import com.binomiaux.archimedes.repository.converter.StudentEntityTransform;
import com.binomiaux.archimedes.repository.entities.PeriodEntity;
import com.binomiaux.archimedes.repository.entities.SchoolEntity;
import com.binomiaux.archimedes.repository.entities.StudentEntity;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;

import com.binomiaux.archimedes.model.Student;

import java.util.Collections;
import java.util.List;

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
        Key key = Key.builder().partitionValue(pk).sortValue("#").build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder().key(key).build();
        StudentEntity record = studentTable.getItem(request);

        return studentRecordTransform.transform(record);
    }

    @Override
    public void create(Student student) {
        // Update school entity
        DynamoDbTable<SchoolEntity> schoolTable = enhancedClient.table(tableName, SchoolEntity.TABLE_SCHEMA);
        SchoolEntity schoolEntity = schoolTable.getItem(r -> r.key(k -> k
            .partitionValue("SCHOOL#" + student.getSchoolCode())
            .sortValue("#")
        ));
        
        if (schoolEntity == null) {
            throw new IllegalArgumentException("School not found");
        }
        
        int nextStudentCode = schoolEntity.getStudentCount() + 1;
        schoolEntity.setStudentCount(nextStudentCode);
        student.setStudentCode(String.valueOf(nextStudentCode));

        // Update teacher entity
        StudentEntity studentEntity = new StudentEntity();
        studentEntity.setPk("STUDENT#" + student.getId());
        studentEntity.setSk("#");
        studentEntity.setType("STUDENT");
        studentEntity.setId(student.getId());
        studentEntity.setSchoolCode(student.getSchoolCode());
        studentEntity.setStudentCode(student.getStudentCode());
        studentEntity.setFirstName(student.getFirstName());
        studentEntity.setLastName(student.getLastName());
        studentEntity.setEmail(student.getEmail());
        studentEntity.setUsername(student.getUsername());
        studentEntity.setAttends(Collections.emptyList());

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
    public void updateStudentPeriods(String studentId, List<String> periods) {
        // Update student entity
        DynamoDbTable<StudentEntity> studentTable = enhancedClient.table(tableName, StudentEntity.TABLE_SCHEMA);
        StudentEntity studentEntity = studentTable.getItem(r -> r.key(k -> k.partitionValue("STUDENT#" + studentId).sortValue("#")));

        studentEntity.setAttends(periods);
        studentTable.updateItem(studentEntity);

        // Update period entity
        DynamoDbTable<PeriodEntity> periodTable = enhancedClient.table(tableName, PeriodEntity.TABLE_SCHEMA);
        for (String periodId : periods) {
            PeriodEntity periodEntity = periodTable.getItem(r -> r.key(k -> k.partitionValue("PERIOD#" + periodId).sortValue("#")));

            List<String> attendedBy = periodEntity.getAttendedBy();
            attendedBy.add(studentId);

            periodEntity.setAttendedBy(attendedBy);
            periodTable.updateItem(periodEntity);
        }
    }
}
