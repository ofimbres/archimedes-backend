package com.binomiaux.archimedes.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.model.TopicTree;
import com.binomiaux.archimedes.service.TopicService;

/**
 * Topic controller.
 */
@RestController
@RequestMapping("/api/v1/topics")
public class TopicController {
    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/")
    public List<TopicTree> get() {
        return topicService.getTopicsHierarchy();
    }
}
