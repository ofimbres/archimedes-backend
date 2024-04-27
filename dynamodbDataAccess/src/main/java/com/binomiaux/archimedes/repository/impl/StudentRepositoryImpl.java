package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.StudentRepository;
import com.binomiaux.archimedes.repository.schema.StudentRecord;
import com.binomiaux.archimedes.repository.transform.StudentRecordTransform;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import com.binomiaux.archimedes.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepositoryImpl implements StudentRepository {

    @Autowired
    private DynamoDbTable<StudentRecord> studentTable;

    private StudentRecordTransform studentRecordTransform = new StudentRecordTransform();

    @Override
    public Student find(String id) {
        String pk = "STUDENT#" + id;
        StudentRecord record = studentTable.getItem(Key.builder()
            .partitionValue(pk)
            .sortValue(pk)
            .build()
        );

        return studentRecordTransform.transform(record);
    }
}
