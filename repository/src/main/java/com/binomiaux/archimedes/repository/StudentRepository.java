package com.binomiaux.archimedes.repository;

import com.binomiaux.archimedes.model.pojo.Student;

public interface StudentRepository {
    Student find(String id);
}
