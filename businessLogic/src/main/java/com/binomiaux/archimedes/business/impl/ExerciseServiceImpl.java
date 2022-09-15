package com.binomiaux.archimedes.business.impl;

import com.binomiaux.archimedes.business.ExerciseService;
import com.binomiaux.archimedes.database.dao.ExerciseDao;
import com.binomiaux.archimedes.model.Exercise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExerciseServiceImpl implements ExerciseService {

    public ExerciseServiceImpl() {
    }

    @Autowired
    private ExerciseDao exerciseDao;

    @Override
    public Exercise getExerciseByCode(String code) {
        Exercise exercise = new Exercise();
        return exercise;
    }

    /*@Override
    public String getLatestExerciseResults(String code, String studentId) {
        DynamoDbClient dynamoDbClient = new DynamoDbClient();
        ExerciseResults exerciseResults = dynamoDbClient.getLatestExerciseResults(studentId, code);

        String filename = code + "_" + studentId + "_" + exerciseResults.getCreatedAt() + ".html";
        return filename;
    }*/

    @Override
    public void createExerciseResults(String code, String studentId, String worksheetCopy, int score, long timestamp) {
        /*S3Client s3Client = new S3Client();
        s3Client.upload(worksheetCopy, studentId, code, timestamp);
        DynamoDbClient dynamoDbClient = new DynamoDbClient();
        dynamoDbClient.setExerciseResults(studentId, code, score, timestamp);*/
    }
}
