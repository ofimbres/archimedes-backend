package com.binomiaux.archimedes.repository.api;

import java.util.List;

import com.binomiaux.archimedes.model.Student;

public interface StudentRepository {
    Student find(String studentId);
    void create(Student student);
    void updateStudentPeriods(String studentId, List<String> periods);
}
