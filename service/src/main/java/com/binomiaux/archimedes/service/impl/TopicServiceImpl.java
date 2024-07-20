package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.model.Topic;
import com.binomiaux.archimedes.model.TopicHierarchy;
import com.binomiaux.archimedes.repository.api.TopicRepository;
import com.binomiaux.archimedes.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TopicServiceImpl implements TopicService {

    @Autowired
    private TopicRepository topicRepository;

    // Cache result in memory
    @Override
    public List<TopicHierarchy> getTopicsHierarchy() {
        List<Topic> parents = topicRepository.findAll();
        List<TopicHierarchy> topicHierarchy = new ArrayList<>();

        parents.stream().forEach(t -> {
            List<TopicHierarchy> descendants = topicRepository.findByTopicId(t.getId())
                    .stream()
                    .map(t2 -> new TopicHierarchy(t2))
                    .collect(Collectors.toList());

            TopicHierarchy th = new TopicHierarchy(t);
            th.setDescendants(descendants);

            topicHierarchy.add(th);
        });
        return topicHierarchy;
    }

    @Override
    public List<Topic> getTopics() {
        return topicRepository.findAll();
    }

    @Override
    public List<Topic> getTopicsById(String topicId) {
        return topicRepository.findByTopicId(topicId);
    }
}
