package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.ActivitySubmissionService;
import com.binomiaux.archimedes.service.awsservices.S3Service;
import com.binomiaux.archimedes.repository.api.ActivitySubmissionRepository;
import com.binomiaux.archimedes.repository.api.ActivityScoreRepository;
import com.binomiaux.archimedes.repository.api.StudentRepository;
import com.binomiaux.archimedes.model.ActivitySubmission;
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
public class ActivitySubmissionServiceImpl implements ActivitySubmissionService {
    private static final Logger log = LoggerFactory.getLogger(ActivitySubmissionService.class);

    @Autowired
    private ActivitySubmissionRepository activityResultRepository;

    @Autowired
    private ActivityScoreRepository activityScoreRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private S3Service s3Service;

    @Value("${s3.activity-results-bucket-name}")
    private String exerciseResultsBucketName;  

    @Override
    public ActivitySubmission createActivitySubmission(ActivitySubmission activitySubmission) {
        // Upload the worksheet content to S3
        String resourcePath = "mini-quiz/" + activitySubmission.getStudent().getStudentId() + "/" + activitySubmission.getActivity().getActivityId() + "/" + System.currentTimeMillis() + ".html";
        activitySubmission.setResourcePath(resourcePath);
        String exerciseResultId = UUID.randomUUID().toString();
        activitySubmission.setActivityResultId(exerciseResultId);

        activityResultRepository.create(activitySubmission);

        Student student = studentRepository.find(activitySubmission.getStudent().getStudentId());
        ActivityScore exerciseScore = activityScoreRepository.findByPeriodAndStudentAndActivity(activitySubmission.getPeriod().getPeriodId(), activitySubmission.getStudent().getStudentId(), activitySubmission.getActivity().getActivityId());

        if (exerciseScore == null) {
            exerciseScore = new ActivityScore();
            exerciseScore.setActivity(activitySubmission.getActivity());
            exerciseScore.setStudent(student);
            exerciseScore.setPeriod(activitySubmission.getPeriod());
            exerciseScore.setTries(1);
            exerciseScore.setScore(activitySubmission.getScore());
            exerciseScore.setActivitySubmission(activitySubmission);
        } else {
            exerciseScore.setActivity(activitySubmission.getActivity());
            exerciseScore.setStudent(student);
            exerciseScore.setPeriod(activitySubmission.getPeriod());
            exerciseScore.setTries(exerciseScore.getTries() + 1);

            if (activitySubmission.getScore() > exerciseScore.getScore()) {
                exerciseScore.setScore(activitySubmission.getScore());
                exerciseScore.setActivitySubmission(activitySubmission);
            }
        }

        activityScoreRepository.create(exerciseScore);
        
        // Upload the file to S3
        s3Service.uploadFile(exerciseResultsBucketName, resourcePath, activitySubmission.getWorksheetContent(), "text/html");

        return activitySubmission;
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