package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.model.ActivityScore;
import com.binomiaux.archimedes.service.ActivitySubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Activity Scores controller.
 */
@RestController
@RequestMapping("/api/v1/activity-scores")
public class ActivityScoreController {

    @Autowired
    private ActivitySubmissionService exerciseResultService;

    @GetMapping("/periods/{periodId}/students/{studentId}")
    public List<ActivityScore> getReportsByPeriodAndStudent(@PathVariable("periodId") String periodId, @PathVariable("studentId") String studentId) {
        return exerciseResultService.getScoresByPeriodAndStudent(periodId, studentId);
    }

    @GetMapping("/periods/{periodId}")
    public List<ActivityScore> getReportsByPeriod(@PathVariable("periodId") String periodId) {
        return exerciseResultService.getScoresByPeriod(periodId);
    }

    @GetMapping("/periods/{periodId}/students/{studentId}/activities/{activityId}")
    public ActivityScore getReportsByPeriodAndStudentAndExercise(@PathVariable("periodId") String periodId, @PathVariable("studentId") String studentId, @PathVariable("activityId") String activityId) {
        return exerciseResultService.getScoresByPeriodAndStudentAndActivity(periodId, studentId, activityId);

    }

    @GetMapping("/periods/{periodId}/activities/{activityId}")
    public List<ActivityScore> getReportsByPeriodAndActivity(@PathVariable("periodId") String periodId, @PathVariable("activityId") String activityId) {
        return exerciseResultService.getScoresByPeriodAndActivity(periodId, activityId);
    }
}
