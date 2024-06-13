package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.StudentRepository;
import com.binomiaux.archimedes.repository.schema.StudentRecord;
import com.binomiaux.archimedes.repository.converter.StudentRecordTransform;

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

    private StudentRecordTransform studentRecordTransform = new StudentRecordTransform();

    @Override
    public Student find(String id) {
        String pk = "STUDENT#" + id;
        DynamoDbTable<StudentRecord> studentTable = enhancedClient.table(tableName, StudentRecord.TABLE_SCHEMA);
        Key key = Key.builder().partitionValue(pk).build();
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder().key(key).build();
        StudentRecord record = studentTable.getItem(request);

        return studentRecordTransform.transform(record);
    }
}
