package com.binomiaux.archimedes.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private TeacherService teacherService;

    @GetMapping("/{teacherId}")
    public Teacher get(@PathVariable String teacherId) {
        return teacherService.getTeacher(teacherId);
    }
}
