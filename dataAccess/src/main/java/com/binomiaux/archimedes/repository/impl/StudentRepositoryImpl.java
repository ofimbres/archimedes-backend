package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.api.StudentRepository;
import com.binomiaux.archimedes.repository.converter.StudentEntityTransform;
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
    public Student find(String id) {
        String pk = "STUDENT#" + id;
        DynamoDbTable<StudentEntity> studentTable = enhancedClient.table(tableName, StudentEntity.TABLE_SCHEMA);
        Key key = Key.builder().partitionValue(pk).build();
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

        // Update teacher entity
        StudentEntity studentEntity = new StudentEntity();
        studentEntity.setPk("SCHOOL#" + student.getSchoolCode() + "#STUDENT#" + nextStudentCode);
        studentEntity.setSk("#");
        studentEntity.setType("STUDENT");
        studentEntity.setCode(String.valueOf(nextStudentCode));
        studentEntity.setFirstName(student.getFirstName());
        studentEntity.setLastName(student.getLastName());
        studentEntity.setEmail(student.getEmail());
        studentEntity.setSchoolCode(student.getSchoolCode());
        studentEntity.setUsername(student.getUsername());

        // TODO
        // If getAttends is null, set a default empty list
        /*List<StudentEntity.Attends> attends = student.getAttends() == null
            ? List.of()
            : student.getAttends().stream().map(this::convertToAttends).collect(Collectors.toList());
        studentEntity.setAttends(attends);*/
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

    /*private StudentEntity.Attends convertToAttends(Student.Attends input) {
        StudentEntity.Attends attends = new StudentEntity.Attends();
        attends.setTeacherCode(input.getTeacherCode());
        attends.setPeriodCode(input.getPeriodCode());
        return attends;
    }*/
}
