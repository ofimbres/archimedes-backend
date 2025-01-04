package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.service.PeriodEnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Period Enrollment controller.
 */
@RestController
@RequestMapping("/api/v1/period-enrollments")
public class PeriodEnrollmentController {

    @Autowired
    private PeriodEnrollmentService periodEnrollmentService;

    @PostMapping("/periods/{periodId}/students/{studentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void enrollStudentInPeriod(@PathVariable("periodId") String periodId, @PathVariable("studentId") String studentId) {
        periodEnrollmentService.enrollStudentInPeriod(studentId, periodId);
    }

    @DeleteMapping("/periods/{periodId}/students/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unenrollStudentInPeriod(@PathVariable("periodId") String periodId, @PathVariable("studentId") String studentId) {
        periodEnrollmentService.unrollStudentInPeriod(studentId, periodId); 
    }
}