package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.model.Topic;
import com.binomiaux.archimedes.repository.TopicRepository;
import com.binomiaux.archimedes.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicServiceImpl implements TopicService {

    @Autowired
    private TopicRepository topicRepository;

    @Override
    public List<Topic> getTopics() {
        return topicRepository.findAll();
    }

    @Override
    public List<Topic> getTopicsById(String topicId) {
        return topicRepository.findByTopicId(topicId);
    }
}
