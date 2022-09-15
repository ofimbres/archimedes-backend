package com.binomiaux.archimedes.database.dao.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.binomiaux.archimedes.database.dao.ExerciseDao;
import com.binomiaux.archimedes.database.transform.ExerciseRecordTransform;
import com.binomiaux.archimedes.database.transform.ScoreRecordTransform;
import com.binomiaux.archimedes.model.Exercise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ExerciseDaoImpl implements ExerciseDao {

    @Autowired
    private DynamoDBMapper mapper;

    private ExerciseRecordTransform exerciseRecordTransform = new ExerciseRecordTransform();

    @Override
    public Exercise getByCode(String code) {
        return null;
    }
}
