package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.StudentService;
import com.binomiaux.archimedes.repository.StudentRepository;
import com.binomiaux.archimedes.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {
    @Autowired
    private StudentRepository studentRepository;

    @Override
    public Student getStudent(String id) {
        return studentRepository.find(id);
    }
}
