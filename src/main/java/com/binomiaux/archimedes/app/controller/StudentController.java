package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.service.StudentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Student controller.
 */
@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/{studentId}")
    public Student get(@PathVariable String studentId) {
        return studentService.getStudentById(studentId);
    }

    @GetMapping("/periods/{periodId}/enrollments")
    public List<Student> getStudentsByPeriod(@PathVariable String periodId) {
        return studentService.getStudentsByPeriod(periodId);
    }
}
