package com.binomiaux.archimedes.controller.jpa;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.entity.Student;
import com.binomiaux.archimedes.service.jpa.StudentJpaService;

/**
 * Test controller to demonstrate new JPA-based student operations
 * Run alongside existing DynamoDB controller for comparison
 */
@RestController
@RequestMapping("/api/v1/students")  // v1 to distinguish from existing
public class StudentJpaController {
    
    @Autowired
    private StudentJpaService studentJpaService;
    
    @PostMapping
    public Student createStudent(
            @RequestParam String studentId,
            @RequestParam String schoolCode,
            @RequestParam String firstName, 
            @RequestParam String lastName,
            @RequestParam String email) {
        
        return studentJpaService.createStudent(studentId, schoolCode, firstName, lastName, email);
    }
    
    @GetMapping("/{studentId}")
    public Student getStudent(@PathVariable String studentId) {
        return studentJpaService.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }
    
    @GetMapping("/school/{schoolCode}")
    public List<Student> getStudentsBySchool(@PathVariable String schoolCode) {
        return studentJpaService.getStudentsBySchool(schoolCode);
    }
    
    @GetMapping("/search")
    public List<Student> searchStudents(
            @RequestParam String schoolCode,
            @RequestParam String searchTerm) {
        
        return studentJpaService.searchStudents(schoolCode, searchTerm);
    }
}