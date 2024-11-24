package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.model.Activity;
import com.binomiaux.archimedes.model.ActivityResult;
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
    @Autowired
    private StudentService studentService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private ActivityService activityService;

    @GetMapping("/periods/{periodId}/scores")
    public ResponseEntity<?> getReportsByPeriod(@PathVariable("periodId") String periodId) {
        throw new UnsupportedOperationException("Not implemented yet.");
        //return ResponseEntity.ok().body(teacherService.getReportsByPeriod(periodId));
    }

    @GetMapping("/periods/{periodId}/activities/{activityId}/scores")
    public ResponseEntity<?> getReportsByPeriodAndExercise(@PathVariable("periodId") String periodId, @PathVariable("activityId") String activityId) {
        throw new UnsupportedOperationException("Not implemented yet.");
        // List<ActivityScore> exerciseScores = exerciseResultService.getByClassAndExercise(periodId, exerciseId);
        // return ResponseEntity.ok().body(exerciseScores);
    }

    // -----
    @GetMapping("/periods/{periodId}/students/{studentId}/scores")
    public ResponseEntity<?> getReportsByStudent(@PathVariable("periodId") String periodId, @PathVariable("studentId") String studentId) {
        throw new UnsupportedOperationException("Not implemented yet.");
        //  //List<ExerciseScore> exerciseScores = exerciseResultService.getByStudent(periodId, studentId);
        //return ResponseEntity.ok().body(studentService.getReportsByStudent(studentId));
    }

    @GetMapping("/periods/{periodId}/students/{studentId}/activities/{activityId}/scores")
    public ResponseEntity<?> getReportsByStudentAndExercise(@PathVariable("periodId") String periodId, @PathVariable("studentId") String studentId, @PathVariable("activityId") String activityId) {
        throw new UnsupportedOperationException("Not implemented yet.");
        //ActivityScore exerciseScore = exerciseResultService.getByStudentAndExercise(periodId, studentId, activityId);
        //return ResponseEntity.ok().body(exerciseScore);

            //     exerciseResultService.getByStudentAndExercise(exerciseId, studentId, exerciseId)
    //     //return ResponseEntity.ok().body(studentService.getReportsByStudentAndExercise(studentId, exerciseId));
    }
}
