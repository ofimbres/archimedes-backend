package com.binomiaux.archimedes.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.service.PeriodService;

/**
 * Period controller with consistent school-scoped design.
 */
@RestController
@RequestMapping("/api/v1/schools/{schoolId}/periods")
public class PeriodController {
    
    @Autowired
    private PeriodService periodService;

    @GetMapping
    public List<Period> getAllPeriodsInSchool(@PathVariable("schoolId") String schoolId) {
        return periodService.getPeriodsBySchool(schoolId);
    }

    @GetMapping("/{periodId}")
    public Period getPeriod(@PathVariable("schoolId") String schoolId,
                           @PathVariable("periodId") String periodId) {
        return periodService.findPeriodInSchool(schoolId, periodId);
    }

    @GetMapping("/teacher/{teacherId}")
    public List<Period> getPeriodsByTeacher(@PathVariable("schoolId") String schoolId,
                                           @PathVariable("teacherId") String teacherId) {
        return periodService.getPeriodsByTeacherInSchool(schoolId, teacherId);
    }

    @GetMapping("/student/{studentId}")
    public List<Period> getPeriodsByStudent(@PathVariable("schoolId") String schoolId,
                                           @PathVariable("studentId") String studentId) {
        return periodService.getPeriodsByStudentInSchool(schoolId, studentId);
    }
}
