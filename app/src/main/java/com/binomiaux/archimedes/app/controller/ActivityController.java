package com.binomiaux.archimedes.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.model.Activity;
import com.binomiaux.archimedes.service.ActivityService;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Exercise controller.
 */
@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @GetMapping("/{activityId}")
    public ResponseEntity<Activity> get(@PathVariable String activityId) {
        return ok(activityService.getActivity(activityId));
    }
}
