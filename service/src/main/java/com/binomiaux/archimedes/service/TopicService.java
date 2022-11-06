package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.Topic;

import java.util.List;

public interface TopicService {
    List<Topic> getTopics();
    List<Topic> getTopicsById(String topicId);
}
