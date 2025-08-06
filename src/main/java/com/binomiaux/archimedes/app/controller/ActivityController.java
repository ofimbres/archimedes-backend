package com.binomiaux.archimedes.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.model.Activity;
import com.binomiaux.archimedes.service.ActivityService;

import java.util.List;

/**
 * Exercise controller.
 */
@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @GetMapping("/{activityId}")
    public Activity getActivity(@PathVariable String activityId) {
        return activityService.getActivity(activityId);
    }

    @GetMapping("topics/{topicId}")
    public List<Activity> getActivitiesByTopic(@PathVariable String topicId) {
        return activityService.getActivitiesByTopicId(topicId);
    }

    @GetMapping("topics/{topicId}/subtopics/{subtopicId}")
    public List<Activity> getActivitiesByTopicAndSubtopic(@PathVariable String topicId, @PathVariable String subtopicId) {
        return activityService.getActivitiesByTopicIdAndSubtopicId(topicId, subtopicId);
    }
}
