package com.binomiaux.archimedes.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.entity.Enrollment;
import com.binomiaux.archimedes.entity.Period;
import com.binomiaux.archimedes.entity.Student;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    // Find by enrollment ID (business key)
    Optional<Enrollment> findByEnrollmentId(String enrollmentId);
    
    // Find by student
    List<Enrollment> findByStudent(Student student);
    List<Enrollment> findByStudentAndStatus(Student student, String status);
    
    // Find by period
    List<Enrollment> findByPeriod(Period period);
    List<Enrollment> findByPeriodAndStatus(Period period, String status);
    
    // Find specific enrollment
    Optional<Enrollment> findByStudentAndPeriod(Student student, Period period);
    
    // Check if student is enrolled in period
    boolean existsByStudentAndPeriodAndStatus(Student student, Period period, String status);
    
    // Custom queries - much simpler than DynamoDB!
    @Query("SELECT e FROM Enrollment e WHERE e.student.school.id = ?1 AND e.status = 'ACTIVE'")
    List<Enrollment> findActiveEnrollmentsBySchool(String schoolId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.period.id = ?1 AND e.status = 'ACTIVE' ORDER BY e.student.lastName, e.student.firstName")
    List<Enrollment> findActiveEnrollmentsByPeriodOrderedByStudent(Long periodId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = ?1 AND e.status = 'ACTIVE' ORDER BY e.period.periodNumber")
    List<Enrollment> findActiveEnrollmentsByStudentOrderedByPeriod(Long studentId);
    
    // Analytics queries
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.period.school.id = ?1 AND e.status = 'ACTIVE'")
    long countActiveEnrollmentsBySchool(String schoolId);
    
    @Query("SELECT e.period.subject, COUNT(e) FROM Enrollment e WHERE e.student.school.id = ?1 AND e.status = 'ACTIVE' GROUP BY e.period.subject")
    List<Object[]> countEnrollmentsBySubjectForSchool(String schoolId);
}