package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.service.PeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Period controller.
 */
@RestController
@RequestMapping("/api/v1/periods")
public class PeriodController {
    
    @Autowired
    private PeriodService periodService;

    @GetMapping("student/{studentId}")
    public List<Period> getPeriodsByStudent(@PathVariable("studentId") String studentId) {
        return periodService.getPeriodsByStudentId(studentId);
    }

    @GetMapping("teachers/{teacherId}")
    public List<Period> getPeriodsByTeacher(@PathVariable("teacherId") String teacherId) {
        return periodService.getPeriodsByTeacherId(teacherId);
    }
}
