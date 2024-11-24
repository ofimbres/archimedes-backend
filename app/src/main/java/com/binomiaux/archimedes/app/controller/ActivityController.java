package com.binomiaux.archimedes.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.app.mapper.ActivityResultMapper;
import com.binomiaux.archimedes.app.request.ActivityResultRequest;
import com.binomiaux.archimedes.model.Activity;
import com.binomiaux.archimedes.model.ActivityResult;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.service.ActivityResultService;
import com.binomiaux.archimedes.service.ActivityService;

import static org.springframework.http.ResponseEntity.ok;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Exercise controller.
 */
@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityResultService activityResultService;

    private ActivityResultMapper mapper = ActivityResultMapper.INSTANCE;

    @GetMapping("/{activityId}")
    public ResponseEntity<Activity> get(@PathVariable String activityId) {
        return ok(activityService.getActivity(activityId));
    }
    
    @PostMapping("{activityId}/results")
    public ResponseEntity<ActivityResult> submitActivityResult(@PathVariable String activityId, @RequestBody ActivityResultRequest activityResultRequest) {
        ActivityResult result = mapper.requestToActivityResult(activityResultRequest, activityId);

        activityResultService.create(result);
        return ok(result);
    }
}
