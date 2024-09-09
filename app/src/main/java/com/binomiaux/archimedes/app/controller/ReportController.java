package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.app.request.CreateReportRequest;
import com.binomiaux.archimedes.model.Exercise;
import com.binomiaux.archimedes.model.ExerciseResult;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.service.ExerciseResultService;
import com.binomiaux.archimedes.service.ExerciseService;
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
    private ExerciseService exerciseService;

    @PostMapping("/")
    public ResponseEntity<ExerciseResult> create(@RequestBody CreateReportRequest request) {
        ExerciseResult exerciseResult = new ExerciseResult();
        Student student = studentService.getStudent(request.getStudentId());
        Exercise exercise = exerciseService.getExercise(request.getExerciseId());
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
