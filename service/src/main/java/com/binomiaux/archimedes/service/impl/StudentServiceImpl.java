package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.StudentService;
import com.binomiaux.archimedes.model.Enrollment;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.api.EnrollmentRepository;
import com.binomiaux.archimedes.repository.api.PeriodRepository;
import com.binomiaux.archimedes.repository.api.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {
    @Autowired
    private PeriodRepository periodRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Override
    public Student getStudentById(String id) {
        return studentRepository.find(id);
    }

    @Override
    public void createStudent(Student student) {
        studentRepository.create(student);
    }

    // @Override
    // public boolean enrollStudentInPeriod(String studentId, String periodId) {
    //     Student student = studentRepository.find(studentId);
    //     Period period = periodRepository.find(periodId);

    //     if (student == null) {
    //         throw new EntityNotFoundException("Student " + studentId + " not found", null);
    //     }

    //     if (period == null) {
    //         throw new EntityNotFoundException("Period " + periodId + " not found", null);
    //     }

    //     Enrollment enrollment = new Enrollment();
    //     enrollment.setStudent(student);
    //     enrollment.setPeriod(period);

    //     enrollmentRepository.create(enrollment);
    //     return true;
    // }

    // @Override
    // public void unrollStudentInPeriod(String studentId, String periodId) {
    //     enrollmentRepository.delete(studentId, periodId);
    // }

    @Override
    public List<Student> getStudentsByPeriod(String periodId) {
        List<Enrollment> enrollments = enrollmentRepository.getEnrollmentsByPeriod(periodId);
        return enrollments.stream()
                  .map(Enrollment::getStudent)
                  .collect(Collectors.toList());
    }
}
