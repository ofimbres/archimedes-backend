package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.model.Activity;
import com.binomiaux.archimedes.model.ActivityResult;
import com.binomiaux.archimedes.model.ActivityScore;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.service.ActivityService;
import com.binomiaux.archimedes.service.ActivityResultService;
import com.binomiaux.archimedes.service.PeriodService;
import com.binomiaux.archimedes.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Exercise Reports controller.
 */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    @Autowired
    private ActivityResultService exerciseResultService;

    @GetMapping("/periods/{periodId}/students/{studentId}/scores")
    public ResponseEntity<?> getReportsByPeriodAndStudent(@PathVariable("periodId") String periodId, @PathVariable("studentId") String studentId) {
        List<ActivityScore> activityScores = exerciseResultService.getScoresByPeriodAndStudent(periodId, studentId);
        return ResponseEntity.ok().body(activityScores);
    }

    @GetMapping("/periods/{periodId}/scores")
    public ResponseEntity<?> getReportsByPeriod(@PathVariable("periodId") String periodId) {
        List<ActivityScore> activityScores = exerciseResultService.getScoresByPeriod(periodId);
        return ResponseEntity.ok().body(activityScores);
    }

    @GetMapping("/periods/{periodId}/students/{studentId}/activities/{activityId}/scores")
    public ResponseEntity<?> getReportsByPeriodAndStudentAndExercise(@PathVariable("periodId") String periodId, @PathVariable("studentId") String studentId, @PathVariable("activityId") String activityId) {
        ActivityScore activityScore = exerciseResultService.getScoresByPeriodAndStudentAndActivity(periodId, studentId, activityId);
        return ResponseEntity.ok().body(activityScore);
    }

    @GetMapping("/periods/{periodId}/activities/{activityId}/scores")
    public ResponseEntity<?> getReportsByPeriodAndActivity(@PathVariable("periodId") String periodId, @PathVariable("activityId") String activityId) {
        List<ActivityScore> activityScores = exerciseResultService.getScoresByPeriodAndActivity(periodId, activityId);
        return ResponseEntity.ok().body(activityScores);
    }
}
