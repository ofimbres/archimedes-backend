package com.binomiaux.archimedes.app.controller;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.dto.request.ActivitySubmissionRequest;
import com.binomiaux.archimedes.model.ActivitySubmission;
import com.binomiaux.archimedes.service.ActivitySubmissionService;

/**
 * Exercise Submission controller.
 */
@RestController
@RequestMapping("/api/v1/activity-submissions")
public class ActivitySubmissionController {

    @Autowired
    private ActivitySubmissionService activitySubmissionService;


    @PostMapping("/")
    public ActivitySubmission createActivitySubmission(@RequestBody ActivitySubmissionRequest activitySubmissionRequest) {
        // Map request to submission directly in controller
        ActivitySubmission submission = new ActivitySubmission();
        submission.setWorksheetContent(activitySubmissionRequest.getWorksheetContent());
        submission.setTimestamp(Instant.now());
        submission.setActivityId(activitySubmissionRequest.getActivityId());
        submission.setStudentId(activitySubmissionRequest.getStudentId());
        submission.setPeriodId(activitySubmissionRequest.getPeriodId());

        activitySubmissionService.createActivitySubmission(submission);
        return submission;
    }
}
