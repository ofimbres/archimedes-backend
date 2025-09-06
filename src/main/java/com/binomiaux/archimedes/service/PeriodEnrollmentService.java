package com.binomiaux.archimedes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.exception.common.EntityNotFoundException;
import com.binomiaux.archimedes.model.Enrollment;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.EnrollmentRepository;
import com.binomiaux.archimedes.repository.PeriodRepository;
import com.binomiaux.archimedes.repository.StudentRepository;

@Service
public class PeriodEnrollmentService {

    @Autowired
    private PeriodRepository periodRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public boolean enrollStudentInPeriod(String studentId, String periodId) {
        Student student = studentRepository.find(studentId);
        Period period = periodRepository.find(periodId);

        if (student == null) {
            throw new EntityNotFoundException("Student " + studentId + " not found", null);
        }

        if (period == null) {
            throw new EntityNotFoundException("Period " + periodId + " not found", null);
        }

        // Create enrollment with simple field assignment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(student.getStudentId());
        enrollment.setPeriodId(period.getPeriodId());
        enrollment.setStudentFirstName(student.getFirstName());
        enrollment.setStudentLastName(student.getLastName());
        enrollment.setPeriodName(period.getName());

        enrollmentRepository.create(enrollment);
        return true;
    }

    public void unrollStudentInPeriod(String studentId, String periodId) {
        enrollmentRepository.delete(studentId, periodId);
    }

    public boolean isStudentEnrolled(String studentId, String periodId) {
        // Check if student is already enrolled by getting their enrollments
        return enrollmentRepository.getEnrollmentsByStudent(studentId)
            .stream()
            .anyMatch(enrollment -> enrollment.getPeriodId().equals(periodId));
    }

    public Enrollment enrollStudent(String studentId, String periodId) {
        Student student = studentRepository.find(studentId);
        Period period = periodRepository.find(periodId);

        if (student == null) {
            throw new EntityNotFoundException("Student " + studentId + " not found", null);
        }

        if (period == null) {
            throw new EntityNotFoundException("Period " + periodId + " not found", null);
        }

        // Create enrollment with simple field assignment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(student.getStudentId());
        enrollment.setPeriodId(period.getPeriodId());
        enrollment.setStudentFirstName(student.getFirstName());
        enrollment.setStudentLastName(student.getLastName());
        enrollment.setPeriodName(period.getName());

        enrollmentRepository.create(enrollment);
        return enrollment;
    }
}
