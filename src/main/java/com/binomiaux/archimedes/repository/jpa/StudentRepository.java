package com.binomiaux.archimedes.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.entity.School;
import com.binomiaux.archimedes.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    // Find by student ID (business key)
    Optional<Student> findByStudentId(String studentId);
    
    // Find by school
    List<Student> findBySchool(School school);
    List<Student> findBySchoolId(Long schoolId);
    List<Student> findBySchoolAndStatus(School school, String status);
    
    // Find by email/username
    Optional<Student> findByEmail(String email);
    Optional<Student> findByUsername(String username);
    
    // Search by name
    List<Student> findBySchoolAndFirstNameContainingIgnoreCase(School school, String firstName);
    List<Student> findBySchoolAndLastNameContainingIgnoreCase(School school, String lastName);
    
    // Find by grade level
    List<Student> findBySchoolAndGradeLevel(School school, Integer gradeLevel);
    
    // Custom queries
    @Query("SELECT s FROM Student s WHERE s.school = ?1 AND s.status = 'ACTIVE' ORDER BY s.lastName, s.firstName")
    List<Student> findActiveStudentsBySchoolOrderedByName(School school);
    
    @Query("SELECT s FROM Student s JOIN s.enrollments e WHERE e.period.id = ?1 AND s.status = 'ACTIVE'")
    List<Student> findStudentsByPeriod(Long periodId);
    
    @Query("SELECT s FROM Student s WHERE s.school = ?1 AND (s.firstName LIKE %?2% OR s.lastName LIKE %?2%)")
    List<Student> searchStudentsByName(School school, String searchTerm);
}