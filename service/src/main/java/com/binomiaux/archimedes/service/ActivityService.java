package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.Activity;

import java.util.List;

public interface ActivityService {
    Activity getActivity(String activityId);
    List<Activity> getActivitiesByTopicId(String topicId);
    List<Activity> getActivitiesByTopicIdAndSubtopicId(String topicId, String subtopicId);
}
