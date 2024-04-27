package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.ExerciseResultService;
import com.binomiaux.archimedes.service.wrappers.S3ClientWrapper;
import com.binomiaux.archimedes.repository.ExerciseResultRepository;
import com.binomiaux.archimedes.model.ExerciseResult;

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
    private S3ClientWrapper s3ClientWrapper;

    @Override
    public void create(ExerciseResult exerciseResult) {
        exerciseResultRepository.create(exerciseResult);
        s3ClientWrapper.uploadWorksheet(exerciseResult.s3Key(), exerciseResult.worksheetContent());
    }

    @Override
    public Iterable<ExerciseResult> getByStudent(String classId, String studentId, String exerciseCode) {
        return null;
    }

    @Override
    public ExerciseResult getByStudentAndExercise(String classId, String studentId, String exerciseCode) {
        return exerciseResultRepository.findByStudentIdAndExerciseCode(classId, studentId, exerciseCode);
    }

    @Override
    public List<ExerciseResult> getByClassAndExercise(String className, String exerciseCode) {
        return exerciseResultRepository.findAllByClassIdAndExerciseCode(className, exerciseCode);
    }
}