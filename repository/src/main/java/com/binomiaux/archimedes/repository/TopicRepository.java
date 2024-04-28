package com.binomiaux.archimedes.repository;

import java.util.List;

import com.binomiaux.archimedes.model.pojo.Topic;

public interface TopicRepository {
    List<Topic> findAll();
    List<Topic> findByTopicId(String topicId);
}
