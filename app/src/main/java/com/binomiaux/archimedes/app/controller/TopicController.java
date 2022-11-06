package com.binomiaux.archimedes.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.http.ResponseEntity.ok;

@RequestMapping("/api/v1/topic")
public class TopicController {
    @GetMapping("/{topicId}/subtopic/{subtopicId}/exercises")
    public ResponseEntity get(@PathVariable String topicId, @PathVariable String subtopicId) {
        return ok("");
    }
}
