package com.binomiaux.archimedes.repository.impl;

import com.binomiaux.archimedes.repository.StudentRepository;
import com.binomiaux.archimedes.repository.converter.StudentEntityConverter;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import com.binomiaux.archimedes.model.dynamodb.StudentEntity;
import com.binomiaux.archimedes.model.pojo.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepositoryImpl implements StudentRepository {

    @Autowired
    private DynamoDbTable<StudentEntity> studentTable;

    private StudentEntityConverter studentRecordTransform = new StudentEntityConverter();

    @Override
    public Student find(String id) {
        String pk = "STUDENT#" + id;
        StudentEntity record = studentTable.getItem(Key.builder()
            .partitionValue(pk)
            .sortValue(pk)
            .build()
        );

        return studentRecordTransform.transform(record);
    }
}
