package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.service.ExerciseService;
import com.binomiaux.archimedes.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Topic controller.
 */
@RestController
@RequestMapping("/api/v1/topic")
public class TopicController {
    @Autowired
    private TopicService topicService;
    @Autowired
    private ExerciseService exerciseService;

    @GetMapping("/")
    public ResponseEntity get() {
        return ok(topicService.getTopicsHierarchy());
    }

    @GetMapping("/{topicId}/exercises")
    public ResponseEntity get(@PathVariable String topicId) {
        var exercises = exerciseService.getExercisesByTopicId(topicId);
        return ok(exercises);
    }

    @GetMapping("/{topicId}/subtopic/{subtopicId}/exercises")
    public ResponseEntity get(@PathVariable String topicId, @PathVariable String subtopicId) {
        var exercises = exerciseService.getExercisesByTopicIdAndSubtopicId(topicId, subtopicId);
        return ok(exercises);
    }
}