package com.binomiaux.archimedes.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.model.ActivityAssignment;
import com.binomiaux.archimedes.service.ActivityAssignmentService;

/**
 * Exercise controller.
 */
@RestController
@RequestMapping("/api/v1/activity-assignments")
public class ActivityAssignmentController {

    @Autowired
    private ActivityAssignmentService activityAssignmentService;

    @PostMapping("/")
    public ActivityAssignment createActivitySubmission(ActivityAssignment activitySubmission) {
        return activityAssignmentService.createActivityAssignment(activitySubmission);
    }

    @DeleteMapping("/periods/{periodId}/activities/{activityId}")
    public void deleteActivitySubmission(@PathVariable String periodId, @PathVariable String activityId) {
        activityAssignmentService.deleteActivityAssignment(periodId, activityId);
    }
}
