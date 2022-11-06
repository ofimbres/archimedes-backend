package com.binomiaux.archimedes.repository.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.binomiaux.archimedes.repository.StudentRepository;
import com.binomiaux.archimedes.repository.schema.StudentRecord;
import com.binomiaux.archimedes.repository.transform.StudentRecordTransform;
import com.binomiaux.archimedes.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepositoryImpl implements StudentRepository {

    @Autowired
    private DynamoDBMapper mapper;

    private StudentRecordTransform studentRecordTransform = new StudentRecordTransform();

    @Override
    public Student find(String id) {
        String pk = "STUDENT#" + id;
        StudentRecord record = mapper.load(StudentRecord.class, pk, pk);

        return studentRecordTransform.transform(record);
    }
}
