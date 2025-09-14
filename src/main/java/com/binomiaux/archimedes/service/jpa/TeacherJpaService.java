package com.binomiaux.archimedes.service.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.binomiaux.archimedes.entity.School;
import com.binomiaux.archimedes.entity.Teacher;
import com.binomiaux.archimedes.exception.common.EntityNotFoundException;
import com.binomiaux.archimedes.repository.jpa.SchoolRepository;
import com.binomiaux.archimedes.repository.jpa.TeacherRepository;

/**
 * Pure JPA-based TeacherService that uses only JPA entities and repositories.
 * No dependencies on DynamoDB services or models.
 */
@Service
@Transactional
public class TeacherJpaService {

    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;

    public TeacherJpaService(TeacherRepository teacherRepository, SchoolRepository schoolRepository) {
        this.teacherRepository = teacherRepository;
        this.schoolRepository = schoolRepository;
    }

    /**
     * Get a teacher by their teacher ID (business identifier)
     */
    public Teacher getTeacherByTeacherId(String teacherId) {
        return teacherRepository.findByTeacherId(teacherId)
            .orElseThrow(() -> new EntityNotFoundException("Teacher not found: " + teacherId, null));
    }

    /**
     * Get a teacher by their database ID
     */
    public Teacher getTeacherById(Long id) {
        return teacherRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Teacher not found with ID: " + id, null));
    }

    /**
     * Get all teachers
     */
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    /**
     * Get teachers by school code
     */
    public List<Teacher> getTeachersBySchoolCode(String schoolCode) {
        School school = getSchoolByCode(schoolCode);
        return teacherRepository.findBySchool(school);
    }

    /**
     * Get active teachers by school code ordered by name
     */
    public List<Teacher> getActiveTeachersBySchoolCode(String schoolCode) {
        School school = getSchoolByCode(schoolCode);
        return teacherRepository.findActiveTeachersBySchoolOrderedByName(school);
    }

    /**
     * Get teachers by school and department
     */
    public List<Teacher> getTeachersBySchoolAndDepartment(String schoolCode, String department) {
        School school = getSchoolByCode(schoolCode);
        return teacherRepository.findBySchoolAndDepartment(school, department);
    }

    /**
     * Search teachers by name within a school
     */
    public List<Teacher> searchTeachersByName(String schoolCode, String searchTerm) {
        School school = getSchoolByCode(schoolCode);
        return teacherRepository.searchTeachersByName(school, searchTerm);
    }

    /**
     * Create a new teacher
     */
    public Teacher createTeacher(String schoolCode, String firstName, String lastName, String email, String department) {
        School school = getSchoolByCode(schoolCode);
        
        // Check if email already exists
        if (email != null && teacherRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Teacher with email already exists: " + email);
        }

        Teacher teacher = new Teacher();
        teacher.setTeacherId(generateTeacherId());
        teacher.setSchool(school);
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setEmail(email);
        teacher.setDepartment(department);
        teacher.setCreatedDate(LocalDate.now());
        teacher.setHireDate(LocalDate.now());
        teacher.setStatus("ACTIVE");

        return teacherRepository.save(teacher);
    }

    /**
     * Update a teacher
     */
    public Teacher updateTeacher(String teacherId, String firstName, String lastName, String email, String department) {
        Teacher teacher = getTeacherByTeacherId(teacherId);
        
        // Check if email is being changed and already exists
        if (email != null && !email.equals(teacher.getEmail())) {
            if (teacherRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("Teacher with email already exists: " + email);
            }
        }
        
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setEmail(email);
        teacher.setDepartment(department);
        
        return teacherRepository.save(teacher);
    }

    /**
     * Update teacher phone
     */
    public Teacher updateTeacherPhone(String teacherId, String phone) {
        Teacher teacher = getTeacherByTeacherId(teacherId);
        teacher.setPhone(phone);
        return teacherRepository.save(teacher);
    }

    /**
     * Deactivate a teacher (soft delete)
     */
    public void deactivateTeacher(String teacherId) {
        Teacher teacher = getTeacherByTeacherId(teacherId);
        teacher.setStatus("INACTIVE");
        teacherRepository.save(teacher);
    }

    /**
     * Activate a teacher
     */
    public Teacher activateTeacher(String teacherId) {
        Teacher teacher = getTeacherByTeacherId(teacherId);
        teacher.setStatus("ACTIVE");
        return teacherRepository.save(teacher);
    }

    /**
     * Delete a teacher permanently
     */
    public void deleteTeacher(String teacherId) {
        Teacher teacher = getTeacherByTeacherId(teacherId);
        teacherRepository.delete(teacher);
    }

    /**
     * Check if teacher exists by teacher ID
     */
    public boolean existsByTeacherId(String teacherId) {
        return teacherRepository.findByTeacherId(teacherId).isPresent();
    }

    /**
     * Check if teacher exists by email
     */
    public boolean existsByEmail(String email) {
        return teacherRepository.findByEmail(email).isPresent();
    }

    /**
     * Get teacher count by school
     */
    public long getTeacherCountBySchoolCode(String schoolCode) {
        School school = getSchoolByCode(schoolCode);
        return teacherRepository.findBySchoolAndStatus(school, "ACTIVE").size();
    }

    // Private helper methods
    private School getSchoolByCode(String schoolCode) {
        return schoolRepository.findBySchoolCode(schoolCode)
            .orElseThrow(() -> new EntityNotFoundException("School not found: " + schoolCode, null));
    }

    private String generateTeacherId() {
        return "TCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}