package com.binomiaux.archimedes.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.service.TeacherService;

/**
 * Teacher controller.
 */
@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherController {
    
    private final TeacherService teacherService;
    
    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/{teacherId}")
    public Teacher get(@PathVariable String teacherId) {
        return teacherService.getTeacher(teacherId);
    }
}
