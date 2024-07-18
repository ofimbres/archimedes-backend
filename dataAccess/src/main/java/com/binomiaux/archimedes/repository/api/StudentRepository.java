package com.binomiaux.archimedes.repository.api;

import com.binomiaux.archimedes.model.Student;

public interface StudentRepository {
    Student find(String id);
    void create(Student student);
}
