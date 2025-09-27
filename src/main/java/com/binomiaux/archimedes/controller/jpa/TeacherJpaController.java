package com.binomiaux.archimedes.controller.jpa;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.dto.mapper.TeacherMapperJpa;
import com.binomiaux.archimedes.dto.response.TeacherResponse;
import com.binomiaux.archimedes.entity.Teacher;
import com.binomiaux.archimedes.service.jpa.TeacherJpaService;

/**
 * Pure JPA-based Teacher controller - completely separate from DynamoDB implementation.
 * Uses different endpoint path to avoid conflicts.
 */
@RestController
// "/api/v1/schools/{schoolId}/teachers"
@RequestMapping("/api/v1/teachers")
public class TeacherJpaController {

    private final TeacherJpaService teacherJpaService;

    public TeacherJpaController(TeacherJpaService teacherJpaService) {
        this.teacherJpaService = teacherJpaService;
    }

    /**
     * Get all teachers
     */
    @GetMapping
    public ResponseEntity<List<TeacherResponse>> getAllTeachers() {
        List<Teacher> teachers = teacherJpaService.getAllTeachers();
        List<TeacherResponse> response = TeacherMapperJpa.toResponseList(teachers);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a teacher by teacher ID
     */
    @GetMapping("/{teacherId}")
    public ResponseEntity<TeacherResponse> getTeacher(@PathVariable String teacherId) {
        Teacher teacher = teacherJpaService.getTeacherByTeacherId(teacherId);
        TeacherResponse response = TeacherMapperJpa.toResponse(teacher);
        return ResponseEntity.ok(response);
    }

    /**
     * Get teachers by school code
     */
    @GetMapping("/school/{schoolCode}")
    public ResponseEntity<List<TeacherResponse>> getTeachersBySchool(@PathVariable Long schoolId) {
        List<Teacher> teachers = teacherJpaService.getTeachersBySchool(schoolId);
        List<TeacherResponse> response = TeacherMapperJpa.toResponseList(teachers);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active teachers by school code
     */
    @GetMapping("/school/{schoolCode}/active")
    public ResponseEntity<List<TeacherResponse>> getActiveTeachersBySchool(@PathVariable Long schoolId) {
        List<Teacher> teachers = teacherJpaService.getActiveTeachersBySchoolId(schoolId);
        List<TeacherResponse> response = TeacherMapperJpa.toResponseList(teachers);
        return ResponseEntity.ok(response);
    }

    /**
     * Get teachers by school and department
     */
    @GetMapping("/school/{schoolCode}/department/{department}")
    public ResponseEntity<List<TeacherResponse>> getTeachersBySchoolAndDepartment(
            @PathVariable Long schoolId,
            @PathVariable String department) {
        List<Teacher> teachers = teacherJpaService.getTeachersBySchoolAndDepartment(schoolId, department);
        List<TeacherResponse> response = TeacherMapperJpa.toResponseList(teachers);
        return ResponseEntity.ok(response);
    }

    /**
     * Search teachers by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<TeacherResponse>> searchTeachers(
            @RequestParam Long schoolId,
            @RequestParam String searchTerm) {
        List<Teacher> teachers = teacherJpaService.searchTeachersByName(schoolId, searchTerm);
        List<TeacherResponse> response = TeacherMapperJpa.toResponseList(teachers);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new teacher
     */
    @PostMapping
    public ResponseEntity<TeacherResponse> createTeacher(
            @RequestParam Long schoolId,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam(required = false) String department) {
        Teacher teacher = teacherJpaService.createTeacher(schoolId, firstName, lastName, email, department);
        TeacherResponse response = TeacherMapperJpa.toResponse(teacher);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a teacher
     */
    @PutMapping("/{teacherId}")
    public ResponseEntity<TeacherResponse> updateTeacher(
            @PathVariable String teacherId,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam(required = false) String department) {
        Teacher teacher = teacherJpaService.updateTeacher(teacherId, firstName, lastName, email, department);
        TeacherResponse response = TeacherMapperJpa.toResponse(teacher);
        return ResponseEntity.ok(response);
    }

    /**
     * Update teacher phone
     */
    @PutMapping("/{teacherId}/phone")
    public ResponseEntity<TeacherResponse> updateTeacherPhone(
            @PathVariable String teacherId,
            @RequestParam String phone) {
        Teacher teacher = teacherJpaService.updateTeacherPhone(teacherId, phone);
        TeacherResponse response = TeacherMapperJpa.toResponse(teacher);
        return ResponseEntity.ok(response);
    }

    /**
     * Activate a teacher
     */
    @PutMapping("/{teacherId}/activate")
    public ResponseEntity<TeacherResponse> activateTeacher(@PathVariable String teacherId) {
        Teacher teacher = teacherJpaService.activateTeacher(teacherId);
        TeacherResponse response = TeacherMapperJpa.toResponse(teacher);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a teacher (soft delete)
     */
    @PutMapping("/{teacherId}/deactivate")
    public ResponseEntity<Void> deactivateTeacher(@PathVariable String teacherId) {
        teacherJpaService.deactivateTeacher(teacherId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a teacher permanently
     */
    @DeleteMapping("/{teacherId}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable String teacherId) {
        teacherJpaService.deleteTeacher(teacherId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get teacher count by school
     */
    @GetMapping("/school/{schoolCode}/count")
    public ResponseEntity<Long> getTeacherCount(@PathVariable Long schoolId) {
        long count = teacherJpaService.getTeacherCountBySchoolId(schoolId);
        return ResponseEntity.ok(count);
    }

    /**
     * Check if teacher exists by teacher ID
     */
    @GetMapping("/{teacherId}/exists")
    public ResponseEntity<Boolean> teacherExists(@PathVariable String teacherId) {
        boolean exists = teacherJpaService.existsByTeacherId(teacherId);
        return ResponseEntity.ok(exists);
    }
}