package com.binomiaux.archimedes.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.dto.mapper.StudentMapper;
import com.binomiaux.archimedes.dto.response.StudentResponse;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.service.StudentService;

/**
 * Student controller with simplified API structure.
 * Uses clean IDs: S001, S002, etc. (scoped per school).
 */
@RestController
@RequestMapping("/api/v1/schools/{schoolId}/students")
public class StudentController {

    private final StudentService studentService;
    
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/{studentId}")
    public StudentResponse get(@PathVariable String schoolId, @PathVariable String studentId) {
        Student student = studentService.getStudentById(schoolId, studentId);
        return StudentMapper.toResponse(student);
    }

    @GetMapping("/{studentId}/periods")
    public List<StudentResponse> getStudentPeriods(@PathVariable String schoolId, @PathVariable String studentId) {
        // This would need implementation in StudentService to return periods for a student
        // For now, return empty list as placeholder
        return List.of();
    }

    @GetMapping("/periods/{periodId}")
    public List<StudentResponse> getStudentsByPeriod(@PathVariable String schoolId, @PathVariable String periodId) {
        List<Student> students = studentService.getStudentsByPeriod(schoolId, periodId);
        return StudentMapper.toResponseList(students);
    }
}
