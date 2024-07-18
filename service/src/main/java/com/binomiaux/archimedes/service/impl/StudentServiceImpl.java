package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.StudentService;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.api.StudentRepository;

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

    @Override
    public void create(Student student) {
        studentRepository.create(student);
    }
}
