package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.api.StudentRepository;
import com.binomiaux.archimedes.repository.entities.SchoolEntity;
import com.binomiaux.archimedes.repository.entities.StudentEntity;
import com.binomiaux.archimedes.repository.exception.EntityNotFoundException;
import com.binomiaux.archimedes.repository.mapper.StudentMapper;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;

import com.binomiaux.archimedes.model.Student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepositoryImpl implements StudentRepository {

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table-name}")
    private String tableName;

    private StudentMapper mapper = StudentMapper.INSTANCE;

    @Override
    public Student find(String studentId) {
        String pk = "STUDENT#" + studentId;
        DynamoDbTable<StudentEntity> studentTable = enhancedClient.table(tableName, StudentEntity.TABLE_SCHEMA);
        Key key = Key.builder().partitionValue(pk).sortValue("#METADATA").build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder().key(key).build();
        StudentEntity record = studentTable.getItem(request);

        return mapper.entityToStudent(record);
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
}
