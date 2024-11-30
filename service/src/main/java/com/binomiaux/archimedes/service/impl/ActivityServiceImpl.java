package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.ActivityService;
import com.binomiaux.archimedes.service.awsservices.S3Service;

import com.binomiaux.archimedes.repository.api.ActivityRepository;
import com.binomiaux.archimedes.model.Activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.net.URL;
import java.time.Duration;

@Service
public class ActivityServiceImpl implements ActivityService {
    private static final Logger log = LoggerFactory.getLogger(ActivityServiceImpl.class);

    @Autowired
    private ActivityRepository exerciseRepository;

    @Autowired
    private S3Service s3Service;

    @Value("${s3.activity-bucket-name}")
    private String exerciseBucketName;

    @Value("${s3.activity-results-bucket-name}")
    private String exerciseResultsBucketName;

    @Value("${mini-quiz-base-url}")
    private String miniQuizBaseUrl;

    @Override
    public Activity getActivity(String activityId) {
        Activity activity = exerciseRepository.findByCode(activityId);
        //URL url = s3Service.generatePresignedUrl(exerciseBucketName, activity.getPath(), Duration.ofMinutes(15));
        //String fileName = activity.getPath().substring(activity.getPath().lastIndexOf('/') + 1); // TODO: Workaround
        //String fullMiniQuizUrl = miniQuizBaseUrl + fileName;
        String fullMiniQuizUrl = miniQuizBaseUrl + activity.getPath();
        activity.setUrl(fullMiniQuizUrl);
        return activity;
    }

    @Override
    public List<Activity> getActivitiesByTopicId(String topicId) {
        return exerciseRepository.findByTopic(topicId);
    }

    @Override
    public List<Activity> getActivitiesByTopicIdAndSubtopicId(String topicId, String subtopicId) {
        return exerciseRepository.findByTopicAndSubtopic(topicId, subtopicId);
    }
}
