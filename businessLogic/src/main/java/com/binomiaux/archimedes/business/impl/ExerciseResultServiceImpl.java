package com.binomiaux.archimedes.business.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.binomiaux.archimedes.business.ExerciseResultService;
import com.binomiaux.archimedes.business.wrappers.S3ClientWrapper;
import com.binomiaux.archimedes.database.repository.ExerciseResultRepository;
import com.binomiaux.archimedes.model.ExerciseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ExerciseResultServiceImpl implements ExerciseResultService {
    @Autowired
    private ExerciseResultRepository exerciseResultRepository;
    @Autowired
    private S3ClientWrapper s3ClientWrapper;

    @Override
    public void create(ExerciseResult exerciseResult) {
        exerciseResultRepository.create(exerciseResult);
        s3ClientWrapper.uploadWorksheet(exerciseResult.getS3Key(), exerciseResult.getWorksheetContent());
    }

    @Override
    public ExerciseResult getByClassStudentAndExercise(String classId, String studentId, String exerciseCode) {
        return exerciseResultRepository.findByClassIdStudentIdAndExerciseCode(classId, studentId, exerciseCode);
    }

    @Override
    public List<ExerciseResult> getByClassAndExercise(String className, String exerciseCode) {
        return exerciseResultRepository.findAllByClassIdAndExerciseCode(className, exerciseCode);
    }
}