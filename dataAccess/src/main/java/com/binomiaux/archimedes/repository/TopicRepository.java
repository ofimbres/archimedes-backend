package com.binomiaux.archimedes.repository;

import com.binomiaux.archimedes.model.Topic;

import java.util.List;

public interface TopicRepository {
    List<Topic> findAll();
    List<Topic> findByTopicId(String topicId);
}
