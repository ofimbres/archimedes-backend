package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.StudentService;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.api.PeriodRepository;
import com.binomiaux.archimedes.repository.api.StudentRepository;
import com.binomiaux.archimedes.repository.exception.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {
    @Autowired
    private PeriodRepository periodRepository;

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

    @Override
    public boolean enrollStudentInPeriod(String studentId, String periodId) {
        if (studentRepository.find(studentId) == null) {
            throw new EntityNotFoundException("Student " + studentId + " not found", null);
        }

        if (periodRepository.find(periodId) == null) {
            throw new EntityNotFoundException("Period " + periodId + " not found", null);
        }

        studentRepository.enrollInPeriod(studentId, periodId);
        return true;
    }

    @Override
    public List<Period> getPeriodsByStudent(String studentId) {
        return periodRepository.getPeriodsByStudent(studentId);
    }
}
