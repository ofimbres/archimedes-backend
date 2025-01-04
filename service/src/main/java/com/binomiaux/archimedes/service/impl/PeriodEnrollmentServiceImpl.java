package com.binomiaux.archimedes.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.repository.api.EnrollmentRepository;
import com.binomiaux.archimedes.repository.api.PeriodRepository;
import com.binomiaux.archimedes.repository.api.StudentRepository;

import com.binomiaux.archimedes.model.Enrollment;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.service.PeriodEnrollmentService;

import com.binomiaux.archimedes.repository.exception.EntityNotFoundException;

@Service
public class PeriodEnrollmentServiceImpl implements PeriodEnrollmentService {

    @Autowired
    private PeriodRepository periodRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Override
    public boolean enrollStudentInPeriod(String studentId, String periodId) {
        Student student = studentRepository.find(studentId);
        Period period = periodRepository.find(periodId);

        if (student == null) {
            throw new EntityNotFoundException("Student " + studentId + " not found", null);
        }

        if (period == null) {
            throw new EntityNotFoundException("Period " + periodId + " not found", null);
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setPeriod(period);

        enrollmentRepository.create(enrollment);
        return true;
    }

    @Override
    public void unrollStudentInPeriod(String studentId, String periodId) {
        enrollmentRepository.delete(studentId, periodId);
    }
}
