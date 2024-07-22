package com.binomiaux.archimedes.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.service.StudentService;

@RestController
@RequestMapping("/api/v1/student")
public class StudentController {
    @Autowired
    private StudentService studentService;

    @PostMapping("/{studentId}/enrollments/{periodId}")
    public ResponseEntity<?> enrollStudentInPeriod(@PathVariable("studentId") String studentId, @PathVariable("periodId") String periodId) {
        boolean isRegistered = studentService.enrollStudentInPeriod(studentId, periodId);
        return ResponseEntity.ok().body("Student registered to period successfully.");
    }
}
