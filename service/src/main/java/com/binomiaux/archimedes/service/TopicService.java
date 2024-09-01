package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.Topic;
import com.binomiaux.archimedes.model.TopicHierarchy;

import java.util.List;

public interface TopicService {
    List<TopicHierarchy> getTopicsHierarchy();
    List<Topic> getTopics();
    List<Topic> getTopicsById(String topicId);
}
