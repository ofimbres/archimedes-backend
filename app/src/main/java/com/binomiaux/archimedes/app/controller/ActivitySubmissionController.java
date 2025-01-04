package com.binomiaux.archimedes.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.app.mapper.ActivityResultMapper;
import com.binomiaux.archimedes.app.request.ActivitySubmissionRequest;
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

    private ActivityResultMapper mapper = ActivityResultMapper.INSTANCE;

    @PostMapping("/")
    public ActivitySubmission createActivitySubmission(@RequestBody ActivitySubmissionRequest activitySubmissionRequest) {
        ActivitySubmission result = mapper.requestToActivityResult(activitySubmissionRequest);

        activitySubmissionService.createActivitySubmission(result);
        return result;
    }
}
