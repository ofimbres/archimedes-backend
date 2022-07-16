package com.binomiaux.archimedes.business.impl;

import com.binomiaux.archimedes.DynamoDbClient;
import com.binomiaux.archimedes.Exercise;
import com.binomiaux.archimedes.ExerciseResults;
import com.binomiaux.archimedes.S3Client;
import com.binomiaux.archimedes.business.ExerciseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ExerciseServiceImpl implements ExerciseService {

    public ExerciseServiceImpl() {
    }

    @Override
    public List<Exercise> getAll() {
        Exercise exercise = new Exercise();
        exercise.setName("Adding Whole Numbers 4");
        exercise.setCode("WN04");
        exercise.setType("miniquiz");
        exercise.setSk("METADATA#WN04");
        exercise.setS3Location("archimedes-mini-quizzes/WN04.htm");

        List<Exercise> output = new ArrayList<>();
        output.add(exercise);
        return output;
    }

    @Override
    public Exercise getExerciseByCode(String code) {
        Exercise exercise = new Exercise();
        exercise.setName("Adding Whole Numbers 4");
        exercise.setCode("WN04");
        exercise.setType("miniquiz");
        exercise.setSk("METADATA#WN04");
        exercise.setS3Location("archimedes-mini-quizzes/WN04.htm");
        return exercise;
    }

    @Override
    public String getLatestExerciseResults(String code, String studentId) {
        DynamoDbClient dynamoDbClient = new DynamoDbClient();
        ExerciseResults exerciseResults = dynamoDbClient.getLatestExerciseResults(studentId, code);

        String filename = code + "_" + studentId + "_" + exerciseResults.getCreatedAt() + ".html";
        return filename;
    }

    @Override
    public void createExerciseResults(String code, String studentId, String worksheetCopy, int score, long timestamp) {
        S3Client s3Client = new S3Client();
        s3Client.upload(worksheetCopy, studentId, code, timestamp);
        DynamoDbClient dynamoDbClient = new DynamoDbClient();
        dynamoDbClient.setExerciseResults(studentId, code, score, timestamp);
    }
}
