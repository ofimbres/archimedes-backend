package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.Student;

import java.util.List;

public interface StudentService {
    void createStudent(Student student);
    Student getStudentById(String id);
    List<Student> getStudentsByPeriod(String periodId);
}
