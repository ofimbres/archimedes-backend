package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.model.Exercise;
import com.binomiaux.archimedes.model.Topic;
import com.binomiaux.archimedes.service.ExerciseService;
import com.binomiaux.archimedes.service.TopicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("/api/v1/topic")
public class TopicController {
    @Autowired
    private TopicService topicService;
    @Autowired
    private ExerciseService exerciseService;

    @GetMapping("/")
    public ResponseEntity get() {
        List<Topic> topics = topicService.getTopics();
        return ok(topics);
    }

    @GetMapping("/{topicId}/subtopic")
    public ResponseEntity get(@PathVariable String topicId) {
        List<Topic> topics = topicService.getTopicsById(topicId);
        return ok(topics);
    }

    @GetMapping("/{topicId}/subtopic/{subtopicId}/exercises")
    public ResponseEntity get(@PathVariable String topicId, @PathVariable String subtopicId) {
        List<Exercise> exercises = exerciseService.getExercisesByTopicIdAndSubtopicId(topicId, subtopicId);
        return ok(exercises);
    }
}
