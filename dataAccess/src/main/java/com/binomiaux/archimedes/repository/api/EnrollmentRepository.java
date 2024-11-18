package com.binomiaux.archimedes.repository.api;

import java.util.List;

import com.binomiaux.archimedes.model.Enrollment;

public interface EnrollmentRepository {
    List<Enrollment> getEnrollmentsByPeriod(String periodId);
    void create(Enrollment enrollment);
    void delete(String studentId, String periodId);
}
