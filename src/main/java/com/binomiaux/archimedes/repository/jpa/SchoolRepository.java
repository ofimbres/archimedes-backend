package com.binomiaux.archimedes.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.entity.School;

@Repository
public interface SchoolRepository extends JpaRepository<School, String> {
    
    // Find by school code (unique identifier)
    Optional<School> findBySchoolCode(String schoolCode);
    
    // Find active schools
    List<School> findByStatus(String status);
    
    // Find by name (partial match)
    List<School> findByNameContainingIgnoreCase(String name);
    
    // Custom queries
    @Query("SELECT s FROM School s WHERE s.status = 'ACTIVE' ORDER BY s.name")
    List<School> findAllActiveSchools();
    
    @Query("SELECT COUNT(st) FROM Student st WHERE st.school.id = ?1 AND st.status = 'ACTIVE'")
    long countActiveStudentsBySchool(String schoolId);
    
    @Query("SELECT COUNT(t) FROM Teacher t WHERE t.school.id = ?1 AND t.status = 'ACTIVE'")
    long countActiveTeachersBySchool(String schoolId);
}