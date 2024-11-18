package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.ActivityService;

import com.binomiaux.archimedes.repository.api.ActivityRepository;
import com.binomiaux.archimedes.model.Activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciseServiceImpl implements ActivityService {
    private static final Logger log = LoggerFactory.getLogger(ExerciseServiceImpl.class);

    @Autowired
    private ActivityRepository exerciseRepository;

    @Override
    public Activity getActivity(String activityId) {
        return exerciseRepository.findByCode(activityId);
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
