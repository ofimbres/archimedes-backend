package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.ActivityResultService;
import com.binomiaux.archimedes.service.awsservices.S3Service;
import com.binomiaux.archimedes.repository.api.ActivityResultRepository;
import com.binomiaux.archimedes.repository.api.ActivityScoreRepository;
import com.binomiaux.archimedes.repository.api.StudentRepository;
import com.binomiaux.archimedes.model.ActivityResult;
import com.binomiaux.archimedes.model.ActivityScore;
import com.binomiaux.archimedes.model.Student;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActivityResultServiceImpl implements ActivityResultService {
    private static final Logger log = LoggerFactory.getLogger(ActivityResultServiceImpl.class);

    @Autowired
    private ActivityResultRepository activityResultRepository;

    @Autowired
    private ActivityScoreRepository activityScoreRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private S3Service s3Service;

    @Value("${s3.activity-results-bucket-name}")
    private String exerciseResultsBucketName;  

    @Override
    public void create(ActivityResult exerciseResult) {
        // Upload the worksheet content to S3
        String resourcePath = "mini-quiz/" + exerciseResult.getStudent().getStudentId() + "/" + exerciseResult.getActivity().getActivityId() + "/" + System.currentTimeMillis() + ".html";
        exerciseResult.setResourcePath(resourcePath);
        String exerciseResultId = UUID.randomUUID().toString();
        exerciseResult.setActivityResultId(exerciseResultId);

        activityResultRepository.create(exerciseResult);

        Student student = studentRepository.find(exerciseResult.getStudent().getStudentId());
        ActivityScore exerciseScore = activityScoreRepository.findByPeriodAndStudentAndActivity(exerciseResult.getPeriod().getPeriodId(), exerciseResult.getStudent().getStudentId(), exerciseResult.getActivity().getActivityId());

        if (exerciseScore == null) {
            exerciseScore = new ActivityScore();
            exerciseScore.setActivity(exerciseResult.getActivity());
            exerciseScore.setStudent(student);
            exerciseScore.setPeriod(exerciseResult.getPeriod());
            exerciseScore.setTries(1);
            exerciseScore.setScore(exerciseResult.getScore());
            exerciseScore.setActivityResult(exerciseResult);
        } else {
            exerciseScore.setActivity(exerciseResult.getActivity());
            exerciseScore.setStudent(student);
            exerciseScore.setPeriod(exerciseResult.getPeriod());
            exerciseScore.setTries(exerciseScore.getTries() + 1);

            if (exerciseResult.getScore() > exerciseScore.getScore()) {
                exerciseScore.setScore(exerciseResult.getScore());
                exerciseScore.setActivityResult(exerciseResult);
            }
        }

        activityScoreRepository.create(exerciseScore);
        
        // Upload the file to S3
        s3Service.uploadFile(exerciseResultsBucketName, resourcePath, exerciseResult.getWorksheetContent(), "text/html");
    }

    @Override
    public List<ActivityScore> getScoresByPeriodAndStudent(String periodId, String studentId) {
        return activityScoreRepository.findByPeriodAndStudent(periodId, studentId);

        // List<ActivityScore> scores = activityScoreRepository.findByPeriod(periodId);
        // return scores.stream()
        //     .mapToInt(ActivityScore::getScore)
        //     .average()
        //     .orElse(0.0);
    }

    @Override
    public List<ActivityScore> getScoresByPeriod(String periodId) {
        return activityScoreRepository.findByPeriod(periodId);

        // List<ActivityScore> scores = activityScoreRepository.findByPeriod(periodId);
        // return scores.stream()
        //     .mapToInt(ActivityScore::getScore)
        //     .average()
        //     .orElse(0.0);
    }

    @Override
    public ActivityScore getScoresByPeriodAndStudentAndActivity(String periodId, String studentId, String activityId) {
        return activityScoreRepository.findByPeriodAndStudentAndActivity(periodId, studentId, activityId);
    }

    @Override
    public List<ActivityScore> getScoresByPeriodAndActivity(String periodId, String activityId) {
        return activityScoreRepository.findByPeriodAndActivity(periodId, activityId);
    }
}