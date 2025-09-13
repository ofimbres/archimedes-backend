package com.binomiaux.archimedes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.dto.request.EnrollmentRequest;
import com.binomiaux.archimedes.service.PeriodEnrollmentService;

/**
 * Period Enrollment controller with simplified API structure.
 * Uses clean IDs: S001 (students), T001-P001 (periods), etc.
 */
@RestController
@RequestMapping("/api/v1/schools/{schoolId}/enrollments")
public class PeriodEnrollmentController {

    @Autowired
    private PeriodEnrollmentService periodEnrollmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void enrollStudent(@PathVariable("schoolId") String schoolId,
                             @RequestBody EnrollmentRequest request) {
        periodEnrollmentService.enrollStudent(schoolId, request.getStudentId(), request.getPeriodId());
    }

    @DeleteMapping("/{studentId}/{periodId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unenrollStudent(@PathVariable("schoolId") String schoolId,
                               @PathVariable("studentId") String studentId, 
                               @PathVariable("periodId") String periodId) {
        periodEnrollmentService.unenrollStudent(schoolId, studentId, periodId); 
    }
}