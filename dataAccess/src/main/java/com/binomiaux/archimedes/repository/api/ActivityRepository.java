package com.binomiaux.archimedes.repository.api;

import com.binomiaux.archimedes.model.Activity;

import java.util.List;

public interface ActivityRepository {
    Activity findByCode(String code);
    List<Activity> findByTopic(String topicId);
    List<Activity> findByTopicAndSubtopic(String topicId, String subtopicId);
}  