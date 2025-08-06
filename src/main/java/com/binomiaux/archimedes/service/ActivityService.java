package com.binomiaux.archimedes.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.model.Activity;
import com.binomiaux.archimedes.repository.ActivityRepository;

@Service
public class ActivityService {
    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

    @Autowired
    private ActivityRepository exerciseRepository;

    @Value("${s3.activity-bucket-name}")
    private String exerciseBucketName;

    @Value("${s3.activity-results-bucket-name}")
    private String exerciseResultsBucketName;

    @Value("${s3.mini-quiz-base-url}")
    private String miniQuizBaseUrl;

    public Activity getActivity(String activityId) {
        Activity activity = exerciseRepository.findByCode(activityId);
        //URL url = s3Service.generatePresignedUrl(exerciseBucketName, activity.getPath(), Duration.ofMinutes(15));
        //String fileName = activity.getPath().substring(activity.getPath().lastIndexOf('/') + 1); // TODO: Workaround
        //String fullMiniQuizUrl = miniQuizBaseUrl + fileName;
        String fullMiniQuizUrl = miniQuizBaseUrl + activity.getPath();
        activity.setUrl(fullMiniQuizUrl);
        return activity;
    }

    public List<Activity> getActivitiesByTopicId(String topicId) {
        return exerciseRepository.findByTopic(topicId);
    }

    public List<Activity> getActivitiesByTopicIdAndSubtopicId(String topicId, String subtopicId) {
        return exerciseRepository.findByTopicAndSubtopic(topicId, subtopicId);
    }
}
