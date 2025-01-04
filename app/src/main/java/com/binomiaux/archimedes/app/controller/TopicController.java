package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.model.TopicHierarchy;
import com.binomiaux.archimedes.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Topic controller.
 */
@RestController
@RequestMapping("/api/v1/topics")
public class TopicController {
    @Autowired
    private TopicService topicService;

    @GetMapping("/")
    public List<TopicHierarchy> get() {
        return topicService.getTopicsHierarchy();
    }
}
