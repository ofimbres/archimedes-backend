package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.Student;

public interface StudentService {
    void create(Student teacher);
    Student getStudent(String id);
}
