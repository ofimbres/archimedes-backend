package com.binomiaux.archimedes.business.impl;

import com.binomiaux.archimedes.business.StudentService;
import com.binomiaux.archimedes.model.Student;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {
    @Override
    public Student getStudent(String id) {
        Student student = new Student();
        student.setId("ofimbres");
        student.setFirstName("Oscar");
        student.setLastName("Fimbres");

        return student;
    }
}
