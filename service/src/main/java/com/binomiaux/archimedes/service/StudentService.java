package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.Student;

public interface StudentService {
    void create(Student student);
    boolean enrollStudentInPeriod(String studentId, String periodId);

    Student getStudent(String id);

    
}
