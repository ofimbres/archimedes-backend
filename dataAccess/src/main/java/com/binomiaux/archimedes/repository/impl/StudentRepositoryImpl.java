package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.api.StudentRepository;
import com.binomiaux.archimedes.repository.converter.StudentEntityTransform;
import com.binomiaux.archimedes.repository.entities.StudentEntity;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;

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
}
