package com.binomiaux.archimedes.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.service.StudentService;
import com.binomiaux.archimedes.service.TeacherService;

@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherController {
    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @GetMapping("/{teacherId}/periods")
    public ResponseEntity<?> getPeriodsByTeacher(@PathVariable("teacherId") String teacherId) {
        List<Period> periods = teacherService.getPeriodsByTeacher(teacherId);
        return ResponseEntity.ok().body(periods);
    }
}
