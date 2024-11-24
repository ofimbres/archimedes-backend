package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.ActivityResultService;
import com.binomiaux.archimedes.service.awsservices.S3Service;
import com.binomiaux.archimedes.repository.api.ActivityResultRepository;
import com.binomiaux.archimedes.repository.api.ActivityScoreRepository;
import com.binomiaux.archimedes.model.ActivityResult;
import com.binomiaux.archimedes.model.ActivityScore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ActivityResultServiceImpl implements ActivityResultService {
    private static final Logger log = LoggerFactory.getLogger(ActivityResultServiceImpl.class);

    @Autowired
    private ActivityResultRepository activityResultRepository;

    @Autowired
    private ActivityScoreRepository activityScoreRepository;

    @Autowired
    private S3Service s3Service;

    @Value("${s3.activity-results-bucket-name}")
    private String exerciseResultsBucketName;  

    @Override
    public void create(ActivityResult exerciseResult) {
        // Upload the worksheet content to S3
        String resourcePath = "mini-quiz/" + exerciseResult.getStudent().getStudentId() + "/" + exerciseResult.getActivity().getActivityId() + "/" + System.currentTimeMillis() + ".html";
        exerciseResult.setResourcePath(resourcePath);

        activityResultRepository.create(exerciseResult);

        ActivityScore exerciseScore = activityScoreRepository.find(exerciseResult.getPeriod().getPeriodId(), exerciseResult.getStudent().getStudentId(), exerciseResult.getActivity().getActivityId());

        if (exerciseScore == null) {
            exerciseScore = new ActivityScore();
            exerciseScore.setActivity(exerciseResult.getActivity());
            exerciseScore.setStudent(exerciseResult.getStudent());
            exerciseScore.setPeriod(exerciseResult.getPeriod());
            exerciseScore.setTries(1);
            exerciseScore.setScore(exerciseResult.getScore());
            exerciseScore.setActivityResult(exerciseResult);
        } else {
            if (exerciseResult.getScore() > exerciseScore.getScore()) {
                exerciseScore.setTries(exerciseScore.getTries() + 1);
                exerciseScore.setScore(exerciseResult.getScore());
                exerciseScore.setActivityResult(exerciseResult);
            }
        }

        activityScoreRepository.create(exerciseScore);
        
        // Upload the file to S3
        s3Service.uploadFile(exerciseResultsBucketName, resourcePath, exerciseResult.getWorksheetContent(), "text/html");
    }

    @Override
    public Iterable<ActivityResult> getByStudent(String classId, String studentId, String exerciseId) {
        return null;
    }

    @Override
    public ActivityScore getByStudentAndExercise(String classId, String studentId, String exerciseId) {
        return null;
        //return activityResultRepository.findByStudentIdAndExerciseId(classId, studentId, exerciseId);
    }

    @Override
    public List<ActivityScore> getByClassAndExercise(String className, String exerciseId) {
        return null;
        //return activityResultRepository.findAllByClassIdAndExerciseId(className, exerciseId);
    }
}