package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.app.request.CreateReportRequest;
import com.binomiaux.archimedes.model.Activity;
import com.binomiaux.archimedes.model.ActivityResult;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.service.ActivityService;
import com.binomiaux.archimedes.service.ExerciseResultService;
import com.binomiaux.archimedes.service.PeriodService;
import com.binomiaux.archimedes.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Exercise Reports controller.
 */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    @Autowired
    private ExerciseResultService exerciseResultService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private ActivityService activityService;

    @PostMapping("/")
    public ResponseEntity<ActivityResult> create(@RequestBody CreateReportRequest request) {
        ActivityResult exerciseResult = new ActivityResult();
        Student student = studentService.getStudent(request.getStudentId());
        Activity exercise = activityService.getActivity(request.getExerciseId());
        Period period = periodService.getPeriod(request.getPeriodId());

        exerciseResult.setExercise(exercise);
        exerciseResult.setStudent(student);
        exerciseResult.setPeriod(period);
        exerciseResult.setScore(request.getScore());
        exerciseResult.setTimestamp(Instant.now());
        exerciseResult.setWorksheetContent(request.getWorksheetContentCopy());

        String id = UUID.randomUUID().toString();
        exerciseResult.setS3Key(id + ".html");

        exerciseResultService.create(exerciseResult);
        return ok(exerciseResult);
    }
}
