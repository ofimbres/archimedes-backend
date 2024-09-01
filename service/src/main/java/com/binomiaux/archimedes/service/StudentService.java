package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;

import java.util.List;

public interface StudentService {
    void create(Student student);
    boolean enrollStudentInPeriod(String studentId, String periodId);

    Student getStudent(String id);
    List<Period> getPeriodsByStudent(String studentId);
}
