package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.ExerciseResultService;
import com.binomiaux.archimedes.service.awsservices.S3Service;
import com.binomiaux.archimedes.repository.api.ExerciseResultRepository;
import com.binomiaux.archimedes.model.ExerciseResult;
import com.binomiaux.archimedes.model.ExerciseScore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciseResultServiceImpl implements ExerciseResultService {
    private static final Logger log = LoggerFactory.getLogger(ExerciseResultServiceImpl.class);

    @Autowired
    private ExerciseResultRepository exerciseResultRepository;
    @Autowired
    private S3Service s3ClientWrapper;

    @Override
    public void create(ExerciseResult exerciseResult) {
        exerciseResultRepository.create(exerciseResult);
        s3ClientWrapper.uploadWorksheet(exerciseResult.getS3Key(), exerciseResult.getWorksheetContent());
    }

    @Override
    public Iterable<ExerciseResult> getByStudent(String classId, String studentId, String exerciseId) {
        return null;
    }

    @Override
    public ExerciseScore getByStudentAndExercise(String classId, String studentId, String exerciseId) {
        return exerciseResultRepository.findByStudentIdAndExerciseId(classId, studentId, exerciseId);
    }

    @Override
    public List<ExerciseScore> getByClassAndExercise(String className, String exerciseId) {
        return exerciseResultRepository.findAllByClassIdAndExerciseId(className, exerciseId);
    }
}