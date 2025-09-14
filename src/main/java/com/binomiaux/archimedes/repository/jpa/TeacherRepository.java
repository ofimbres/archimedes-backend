package com.binomiaux.archimedes.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.entity.School;
import com.binomiaux.archimedes.entity.Teacher;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    
    // Find by teacher ID (business key)
    Optional<Teacher> findByTeacherId(String teacherId);
    
    // Find by school
    List<Teacher> findBySchool(School school);
    List<Teacher> findBySchoolId(String schoolId);
    List<Teacher> findBySchoolAndStatus(School school, String status);
    
    // Find by email
    Optional<Teacher> findByEmail(String email);
    
    // Find by department
    List<Teacher> findBySchoolAndDepartment(School school, String department);
    
    // Search by name
    List<Teacher> findBySchoolAndFirstNameContainingIgnoreCase(School school, String firstName);
    List<Teacher> findBySchoolAndLastNameContainingIgnoreCase(School school, String lastName);
    
    // Custom queries
    @Query("SELECT t FROM Teacher t WHERE t.school = ?1 AND t.status = 'ACTIVE' ORDER BY t.lastName, t.firstName")
    List<Teacher> findActiveTeachersBySchoolOrderedByName(School school);
    
    @Query("SELECT t FROM Teacher t WHERE t.school = ?1 AND (t.firstName LIKE %?2% OR t.lastName LIKE %?2%)")
    List<Teacher> searchTeachersByName(School school, String searchTerm);
    
    @Query("SELECT DISTINCT t.department FROM Teacher t WHERE t.school = ?1 AND t.department IS NOT NULL")
    List<String> findDepartmentsBySchool(School school);
}