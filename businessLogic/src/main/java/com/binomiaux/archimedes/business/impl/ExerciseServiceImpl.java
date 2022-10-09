package com.binomiaux.archimedes.business.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.binomiaux.archimedes.business.ExerciseService;
import com.binomiaux.archimedes.database.repository.ExerciseRepository;
import com.binomiaux.archimedes.model.Exercise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExerciseServiceImpl implements ExerciseService {

    @Autowired
    private ExerciseRepository exerciseDao;

    @Override
    public Exercise getExercise(String id) {
        Exercise exercise = new Exercise();
        exercise.setId("WN16");
        exercise.setName("Multiplying Whole Numbers");
        exercise.setClassification("miniquiz");
        return exercise;
    }
}
