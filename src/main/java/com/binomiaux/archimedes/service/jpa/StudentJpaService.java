package com.binomiaux.archimedes.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.binomiaux.archimedes.entity.Enrollment;
import com.binomiaux.archimedes.entity.School;
import com.binomiaux.archimedes.entity.Student;
import com.binomiaux.archimedes.repository.jpa.EnrollmentRepository;
import com.binomiaux.archimedes.repository.jpa.SchoolRepository;
import com.binomiaux.archimedes.repository.jpa.StudentRepository;

/**
 * Simple student service demonstrating PostgreSQL/JPA ease of use.
 * Compare this to the complexity of DynamoDB services!
 */
@Service
@Transactional
public class StudentJpaService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private SchoolRepository schoolRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    // Simple CRUD operations
    public Student createStudent(String studentId, Long schoolId, String firstName, String lastName, String email) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("School not found: " + schoolId));
        
        Student student = new Student(studentId, school, firstName, lastName);
        student.setEmail(email);
        
        return studentRepository.save(student);
    }
    
    public Optional<Student> findByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }
    
    public List<Student> getStudentsBySchool(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("School not found: " + schoolId));
        
        return studentRepository.findActiveStudentsBySchoolOrderedByName(school);
    }
    
    public List<Student> searchStudents(Long schoolId, String searchTerm) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("School not found: " + schoolId));
        
        return studentRepository.searchStudentsByName(school, searchTerm);
    }
    
    public List<Enrollment> getStudentEnrollments(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        return enrollmentRepository.findActiveEnrollmentsByStudentOrderedByPeriod(student.getId());
    }
    
    // Look how simple this is compared to DynamoDB!
    public List<Student> getStudentsInPeriod(Long periodId) {
        return studentRepository.findStudentsByPeriod(periodId);
    }
    
    public void updateStudentInfo(String studentId, String email, String phone) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        student.setEmail(email);
        student.setPhone(phone);
        
        studentRepository.save(student); // That's it! No complex key management
    }
    
    public void deactivateStudent(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        student.setStatus("INACTIVE");
        studentRepository.save(student);
    }
}