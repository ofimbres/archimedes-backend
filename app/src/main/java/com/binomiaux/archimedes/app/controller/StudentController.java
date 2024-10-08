package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

/**
 * Student controller.
 */
@RestController
@RequestMapping("/api/v1/student")
public class StudentController {

    @Autowired
    private StudentService studentService;


    @GetMapping("/{studentId}")
    public ResponseEntity<Student> get(@PathVariable String studentId) {
        return ok(studentService.getStudent(studentId));
    }

    @PostMapping("/{studentId}/enrollment/{periodId}")
    public ResponseEntity<?> enrollStudentInPeriod(@PathVariable("studentId") String studentId,
            @PathVariable("periodId") String periodId) {
        studentService.enrollStudentInPeriod(studentId, periodId);
        return ResponseEntity.ok().body("Student registered to period successfully.");
    }

    @GetMapping("/{studentId}/periods")
    public ResponseEntity<?> getPeriodsByStudent(@PathVariable("studentId") String studentId) {
        List<Period> periods = studentService.getPeriodsByStudent(studentId);
        return ResponseEntity.ok().body(periods);
    }
}

