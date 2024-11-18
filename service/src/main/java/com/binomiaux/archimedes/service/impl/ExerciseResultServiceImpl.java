package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.ExerciseResultService;
import com.binomiaux.archimedes.service.awsservices.S3Service;
import com.binomiaux.archimedes.repository.api.ExerciseResultRepository;
import com.binomiaux.archimedes.repository.api.ExerciseScoreRepository;
import com.binomiaux.archimedes.model.ActivityResult;
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
    private ExerciseScoreRepository exerciseScoreRepository;

    @Autowired
    private S3Service s3Service;

    @Override
    public void create(ActivityResult exerciseResult) {
        exerciseResultRepository.create(exerciseResult);

        ExerciseScore exerciseScore = exerciseScoreRepository.find(exerciseResult.getPeriod().getPeriodId(), exerciseResult.getStudent().getStudentId(), exerciseResult.getExercise().getActivityId());

        if (exerciseScore == null) {
            exerciseScore = new ExerciseScore();
            exerciseScore.setExercise(exerciseResult.getExercise());
            exerciseScore.setStudent(exerciseResult.getStudent());
            exerciseScore.setPeriod(exerciseResult.getPeriod());
            exerciseScore.setTries(1);
            exerciseScore.setScore(exerciseResult.getScore());
            exerciseScore.setExerciseResult(exerciseResult.getS3Key());
        } else {
            if (exerciseResult.getScore() > exerciseScore.getScore()) {
                exerciseScore.setTries(exerciseScore.getTries() + 1);
                exerciseScore.setScore(exerciseResult.getScore());
                exerciseScore.setExerciseResult(exerciseResult.getS3Key());
            }
        }

        exerciseScoreRepository.create(exerciseScore);

        s3Service.uploadWorksheet(exerciseResult.getS3Key(), exerciseResult.getWorksheetContent());
    }

    @Override
    public Iterable<ActivityResult> getByStudent(String classId, String studentId, String exerciseId) {
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