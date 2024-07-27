package com.binomiaux.archimedes.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.service.TeacherService;

@RestController
@RequestMapping("/api/v1/teacher")
public class TeacherController {
    @Autowired
    private TeacherService teacherService;

    @GetMapping("/{teacherId}/periods")
    public ResponseEntity<?> getPeriodsByTeacher(@PathVariable("teacherId") String teacherId) {
        List<Period> periods = teacherService.getPeriodsByTeacher(teacherId);
        return ResponseEntity.ok().body(periods);
    }

    @GetMapping("/{teacherId}/period/{periodId}/students")
    public ResponseEntity<?> getStudentsByPeriod(@PathVariable("teacherId") String teacherId, @PathVariable("periodId") String periodId) {
        List<Student> students = teacherService.getStudentsByPeriod(periodId);
        return ResponseEntity.ok().body(students);
    }
}
