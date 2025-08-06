package com.binomiaux.archimedes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.exception.common.EntityNotFoundException;
import com.binomiaux.archimedes.model.Enrollment;
import com.binomiaux.archimedes.model.School;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.EnrollmentRepository;
import com.binomiaux.archimedes.repository.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SchoolService schoolService;

    public StudentService(StudentRepository studentRepository, 
                         EnrollmentRepository enrollmentRepository,
                         SchoolService schoolService) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.schoolService = schoolService;
    }

    public Student getStudentById(String id) {
        Student student = studentRepository.find(id);
        if (student == null) {
            throw new EntityNotFoundException("Student " + id + " not found", null);
        }
        return student;
    }

    public void createStudent(Student student) {
        // Business logic validation - check if school exists
        School school = schoolService.getSchool(student.getSchoolId());
        if (school == null) {
            throw new EntityNotFoundException("School " + student.getSchoolId() + " not found", null);
        }
        
        // Let repository handle the creation
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

    public List<Student> getStudentsByPeriod(String periodId) {
        List<Enrollment> enrollments = enrollmentRepository.getEnrollmentsByPeriod(periodId);
        return enrollments.stream()
                  .map(enrollment -> {
                      // For now, create a Student from Enrollment data
                      Student student = new Student();
                      student.setStudentId(enrollment.getStudentId());
                      student.setFirstName(enrollment.getStudentFirstName());
                      student.setLastName(enrollment.getStudentLastName());
                      return student;
                  })
                  .collect(Collectors.toList());
    }
}
