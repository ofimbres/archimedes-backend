package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.service.StudentService;
import com.binomiaux.archimedes.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

/**
 * Period controller.
 */
@RestController
@RequestMapping("/api/v1/periods")
public class PeriodController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;


    @GetMapping("/{periodId}/students")
    public ResponseEntity<List<Student>> get(@PathVariable String periodId) {
        List<Student> students = teacherService.getStudentsByPeriod(periodId);
        return ResponseEntity.ok().body(students);
    }

    @PostMapping("/{periodId}/students/{studentId}")
    public ResponseEntity<?> enrollStudentInPeriod(@PathVariable("periodId") String periodId, @PathVariable("studentId") String studentId) {
        studentService.enrollStudentInPeriod(studentId, periodId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Student added to period successfully.");
    }

    @DeleteMapping("/{periodId}/students/{studentId}")
    public ResponseEntity<?> unenrollStudentInPeriod(@PathVariable("periodId") String periodId, @PathVariable("studentId") String studentId) {
        studentService.enrollStudentInPeriod(studentId, periodId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{periodId}/reports")
    public ResponseEntity<?> getReportsByPeriod(@PathVariable("periodId") String periodId) {
        throw new UnsupportedOperationException("Not implemented yet.");
        //return ResponseEntity.ok().body(teacherService.getReportsByPeriod(periodId));
    }

    @GetMapping("/{periodId}/exercises/{exerciseId}/reports")
    public ResponseEntity<?> getReportsByPeriodAndExercise(@PathVariable("periodId") String periodId, @PathVariable("exerciseId") String exerciseId) {
        throw new UnsupportedOperationException("Not implemented yet.");
        //return ResponseEntity.ok().body(teacherService.getReportsByPeriodAndExercise(periodId, exerciseId));
    }
}
