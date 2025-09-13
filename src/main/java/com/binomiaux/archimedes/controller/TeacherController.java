package com.binomiaux.archimedes.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.dto.mapper.TeacherMapper;
import com.binomiaux.archimedes.dto.response.TeacherResponse;
import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.service.TeacherService;

/**
 * Teacher controller with simplified API structure.  
 * Uses clean IDs: T001, T002, etc. (scoped per school).
 */
@RestController
@RequestMapping("/api/v1/schools/{schoolId}/teachers")
public class TeacherController {
    
    private final TeacherService teacherService;
    
    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/{teacherId}")
    public TeacherResponse get(@PathVariable String schoolId, @PathVariable String teacherId) {
        Teacher teacher = teacherService.getTeacher(schoolId, teacherId);
        return TeacherMapper.toResponse(teacher);
    }

    @GetMapping("/{teacherId}/periods")
    public TeacherResponse getTeacherPeriods(@PathVariable String schoolId, @PathVariable String teacherId) {
        // This would return periods for a specific teacher
        Teacher teacher = teacherService.getTeacher(schoolId, teacherId);
        return TeacherMapper.toResponse(teacher);
    }
}
