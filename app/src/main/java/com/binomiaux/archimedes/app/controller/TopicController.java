package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.model.Activity;
import com.binomiaux.archimedes.model.TopicHierarchy;
import com.binomiaux.archimedes.service.ActivityService;
import com.binomiaux.archimedes.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

/**
 * Topic controller.
 */
@RestController
@RequestMapping("/api/v1/topics")
public class TopicController {
    @Autowired
    private TopicService topicService;
    @Autowired
    private ActivityService activityService;

    @GetMapping("/")
    public ResponseEntity<List<TopicHierarchy>> get() {
        return ok(topicService.getTopicsHierarchy());
    }

    @GetMapping("/{topicId}/activities")
    public ResponseEntity<List<Activity>> get(@PathVariable String topicId) {
        var activities = activityService.getActivitiesByTopicId(topicId);
        return ok(activities);
    }

    @GetMapping("/{topicId}/subtopics/{subtopicId}/activities")
    public ResponseEntity<List<Activity>> get(@PathVariable String topicId, @PathVariable String subtopicId) {
        var activities = activityService.getActivitiesByTopicIdAndSubtopicId(topicId, subtopicId);
        return ok(activities);
    }
}
