package com.binomiaux.archimedes.database.repository.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.binomiaux.archimedes.database.repository.ExerciseRepository;
import com.binomiaux.archimedes.database.transform.ExerciseRecordTransform;
import com.binomiaux.archimedes.model.Exercise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExerciseRepositoryImpl implements ExerciseRepository {

    @Autowired
    private DynamoDBMapper mapper;

    private ExerciseRecordTransform exerciseRecordTransform = new ExerciseRecordTransform();

    @Override
    public Exercise findByCode(String code) {
        return null;
    }

    @Override
    public List<Exercise> findAll() {
        return null;
    }
}
