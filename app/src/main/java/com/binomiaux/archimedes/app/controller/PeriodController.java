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


    @GetMapping("/{periodId}/students/enrollments")
    public ResponseEntity<List<Student>> get(@PathVariable String periodId) {
        List<Student> students = teacherService.getStudentsByPeriod(periodId);
        return ResponseEntity.ok().body(students);
    }

    @PostMapping("/{periodId}/students/{studentId}/enrollments")
    public ResponseEntity<?> enrollStudentInPeriod(@PathVariable("periodId") String periodId, @PathVariable("studentId") String studentId) {
        studentService.enrollStudentInPeriod(studentId, periodId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Student added to period successfully.");
    }

    @DeleteMapping("/{periodId}/students/{studentId}/enrollments")
    public ResponseEntity<?> unenrollStudentInPeriod(@PathVariable("periodId") String periodId, @PathVariable("studentId") String studentId) {
        studentService.enrollStudentInPeriod(studentId, periodId);
        return ResponseEntity.noContent().build();
    }
}
