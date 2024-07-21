package com.binomiaux.archimedes.repository.api;

import com.binomiaux.archimedes.model.Student;

public interface StudentRepository {
    Student find(String studentId);
    void create(Student student);
    void enrollInPeriod(String studentId, String periodId);
}
