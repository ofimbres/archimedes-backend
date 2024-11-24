package com.binomiaux.archimedes.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.service.TeacherService;

/**
 * Teacher controller.
 */
@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherController {
    @Autowired
    private TeacherService teacherService;

    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @GetMapping("/{studentId}")
    public ResponseEntity<Teacher> get(@PathVariable String teacherId) {
        return ResponseEntity.ok(teacherService.getTeacher(teacherId));
    }

    @GetMapping("/{teacherId}/periods")
    public ResponseEntity<?> getPeriodsByTeacher(@PathVariable("teacherId") String teacherId) {
        List<Period> periods = teacherService.getPeriodsByTeacher(teacherId);
        return ResponseEntity.ok().body(periods);
    }
}
