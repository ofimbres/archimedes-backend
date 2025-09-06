package com.binomiaux.archimedes.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.service.StudentService;

/**
 * Student controller.
 */
@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;
    
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/{studentId}")
    public Student get(@PathVariable String studentId) {
        return studentService.getStudentById(studentId);
    }

    @GetMapping("/periods/{periodId}/enrollments")
    public List<Student> getStudentsByPeriod(@PathVariable String periodId) {
        return studentService.getStudentsByPeriod(periodId);
    }
}
