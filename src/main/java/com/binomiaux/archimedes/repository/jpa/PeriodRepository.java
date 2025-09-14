package com.binomiaux.archimedes.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.entity.Period;
import com.binomiaux.archimedes.entity.School;
import com.binomiaux.archimedes.entity.Teacher;

@Repository
public interface PeriodRepository extends JpaRepository<Period, Long> {
    
    // Find by period ID (business key)
    Optional<Period> findByPeriodId(String periodId);
    
    // Find by school
    List<Period> findBySchool(School school);
    List<Period> findBySchoolId(String schoolId);
    List<Period> findBySchoolAndStatus(School school, String status);
    
    // Find by teacher
    List<Period> findByTeacher(Teacher teacher);
    List<Period> findByTeacherAndStatus(Teacher teacher, String status);
    
    // Find by subject
    List<Period> findBySchoolAndSubject(School school, String subject);
    
    // Find by period number
    List<Period> findBySchoolAndPeriodNumber(School school, Integer periodNumber);
    
    // Find by academic year/semester
    List<Period> findBySchoolAndAcademicYear(School school, String academicYear);
    List<Period> findBySchoolAndSemester(School school, String semester);
    
    // Custom queries
    @Query("SELECT p FROM Period p WHERE p.school = ?1 AND p.status = 'ACTIVE' ORDER BY p.periodNumber, p.name")
    List<Period> findActivePeriodsOrderedByNumber(School school);
    
    @Query("SELECT p FROM Period p JOIN p.enrollments e WHERE e.student.id = ?1 AND p.status = 'ACTIVE'")
    List<Period> findPeriodsByStudent(Long studentId);
    
    @Query("SELECT DISTINCT p.subject FROM Period p WHERE p.school = ?1 AND p.subject IS NOT NULL")
    List<String> findSubjectsBySchool(School school);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.period.id = ?1 AND e.status = 'ACTIVE'")
    long countActiveEnrollmentsByPeriod(Long periodId);
}