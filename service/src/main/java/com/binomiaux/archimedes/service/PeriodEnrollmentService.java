package com.binomiaux.archimedes.service;

public interface PeriodEnrollmentService {
    boolean enrollStudentInPeriod(String studentId, String periodId);
    void unrollStudentInPeriod(String studentId, String periodId);
}
