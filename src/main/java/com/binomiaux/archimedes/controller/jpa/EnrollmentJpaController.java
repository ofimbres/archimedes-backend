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

import com.binomiaux.archimedes.dto.mapper.EnrollmentMapperJpa;
import com.binomiaux.archimedes.dto.response.EnrollmentResponse;
import com.binomiaux.archimedes.entity.Enrollment;
import com.binomiaux.archimedes.service.jpa.EnrollmentJpaService;

/**
 * Pure JPA-based Enrollment controller - completely separate from DynamoDB implementation.
 * Uses different endpoint path to avoid conflicts.
 */
@RestController
@RequestMapping("/api/v1/enrollments")
public class EnrollmentJpaController {

    private final EnrollmentJpaService enrollmentJpaService;

    public EnrollmentJpaController(EnrollmentJpaService enrollmentJpaService) {
        this.enrollmentJpaService = enrollmentJpaService;
    }

    /**
     * Get all enrollments
     */
    @GetMapping
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentJpaService.getAllEnrollments();
        List<EnrollmentResponse> response = EnrollmentMapperJpa.toResponseList(enrollments);
        return ResponseEntity.ok(response);
    }

    /**
     * Get an enrollment by enrollment ID
     */
    @GetMapping("/{enrollmentId}")
    public ResponseEntity<EnrollmentResponse> getEnrollment(@PathVariable String enrollmentId) {
        Enrollment enrollment = enrollmentJpaService.getEnrollmentByEnrollmentId(enrollmentId);
        EnrollmentResponse response = EnrollmentMapperJpa.toResponse(enrollment);
        return ResponseEntity.ok(response);
    }

    /**
     * Get enrollments by student ID
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByStudent(@PathVariable String studentId) {
        List<Enrollment> enrollments = enrollmentJpaService.getEnrollmentsByStudentId(studentId);
        List<EnrollmentResponse> response = EnrollmentMapperJpa.toResponseList(enrollments);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active enrollments by student ID (student's schedule)
     */
    @GetMapping("/student/{studentId}/active")
    public ResponseEntity<List<EnrollmentResponse>> getActiveEnrollmentsByStudent(@PathVariable String studentId) {
        List<Enrollment> enrollments = enrollmentJpaService.getActiveEnrollmentsByStudentIdOrderedByPeriod(studentId);
        List<EnrollmentResponse> response = enrollments.stream()
                .map(EnrollmentMapperJpa::toStudentResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Get enrollments by period ID
     */
    @GetMapping("/period/{periodId}")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByPeriod(@PathVariable String periodId) {
        List<Enrollment> enrollments = enrollmentJpaService.getEnrollmentsByPeriodId(periodId);
        List<EnrollmentResponse> response = EnrollmentMapperJpa.toResponseList(enrollments);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active enrollments by period ID (class roster)
     */
    @GetMapping("/period/{periodId}/active")
    public ResponseEntity<List<EnrollmentResponse>> getActiveEnrollmentsByPeriod(@PathVariable String periodId) {
        List<Enrollment> enrollments = enrollmentJpaService.getActiveEnrollmentsByPeriodIdOrderedByStudent(periodId);
        List<EnrollmentResponse> response = enrollments.stream()
                .map(EnrollmentMapperJpa::toPeriodResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Get active enrollments by school code
     */
    @GetMapping("/school/{schoolCode}/active")
    public ResponseEntity<List<EnrollmentResponse>> getActiveEnrollmentsBySchool(@PathVariable String schoolCode) {
        List<Enrollment> enrollments = enrollmentJpaService.getActiveEnrollmentsBySchoolCode(schoolCode);
        List<EnrollmentResponse> response = EnrollmentMapperJpa.toResponseList(enrollments);
        return ResponseEntity.ok(response);
    }

    /**
     * Enroll a student in a period
     */
    @PostMapping
    public ResponseEntity<EnrollmentResponse> enrollStudent(
            @RequestParam String studentId,
            @RequestParam String periodId) {
        Enrollment enrollment = enrollmentJpaService.enrollStudent(studentId, periodId);
        EnrollmentResponse response = EnrollmentMapperJpa.toResponse(enrollment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Bulk enroll students in a period
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<EnrollmentResponse>> bulkEnrollStudents(
            @RequestParam List<String> studentIds,
            @RequestParam String periodId) {
        List<Enrollment> enrollments = enrollmentJpaService.bulkEnrollStudents(studentIds, periodId);
        List<EnrollmentResponse> response = EnrollmentMapperJpa.toResponseList(enrollments);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update enrollment grade
     */
    @PutMapping("/{enrollmentId}/grade")
    public ResponseEntity<EnrollmentResponse> updateEnrollmentGrade(
            @PathVariable String enrollmentId,
            @RequestParam String grade) {
        Enrollment enrollment = enrollmentJpaService.updateEnrollmentGrade(enrollmentId, grade);
        EnrollmentResponse response = EnrollmentMapperJpa.toResponse(enrollment);
        return ResponseEntity.ok(response);
    }

    /**
     * Reactivate an enrollment
     */
    @PutMapping("/{enrollmentId}/reactivate")
    public ResponseEntity<EnrollmentResponse> reactivateEnrollment(@PathVariable String enrollmentId) {
        Enrollment enrollment = enrollmentJpaService.reactivateEnrollment(enrollmentId);
        EnrollmentResponse response = EnrollmentMapperJpa.toResponse(enrollment);
        return ResponseEntity.ok(response);
    }

    /**
     * Unenroll a student from a period (soft delete)
     */
    @PutMapping("/unenroll")
    public ResponseEntity<Void> unenrollStudent(
            @RequestParam String studentId,
            @RequestParam String periodId) {
        enrollmentJpaService.unenrollStudent(studentId, periodId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete an enrollment permanently
     */
    @DeleteMapping("/{enrollmentId}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable String enrollmentId) {
        enrollmentJpaService.deleteEnrollment(enrollmentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if a student is enrolled in a period
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkEnrollment(
            @RequestParam String studentId,
            @RequestParam String periodId) {
        boolean isEnrolled = enrollmentJpaService.isStudentEnrolledInPeriod(studentId, periodId);
        return ResponseEntity.ok(isEnrolled);
    }

    /**
     * Get specific enrollment between student and period
     */
    @GetMapping("/student/{studentId}/period/{periodId}")
    public ResponseEntity<EnrollmentResponse> getEnrollmentByStudentAndPeriod(
            @PathVariable String studentId,
            @PathVariable String periodId) {
        Enrollment enrollment = enrollmentJpaService.getEnrollmentByStudentAndPeriod(studentId, periodId);
        EnrollmentResponse response = EnrollmentMapperJpa.toResponse(enrollment);
        return ResponseEntity.ok(response);
    }

    /**
     * Get enrollment count for a period
     */
    @GetMapping("/period/{periodId}/count")
    public ResponseEntity<Long> getEnrollmentCountForPeriod(@PathVariable String periodId) {
        long count = enrollmentJpaService.getEnrollmentCountForPeriod(periodId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get enrollment count for a school
     */
    @GetMapping("/school/{schoolCode}/count")
    public ResponseEntity<Long> getEnrollmentCountForSchool(@PathVariable String schoolCode) {
        long count = enrollmentJpaService.getEnrollmentCountForSchool(schoolCode);
        return ResponseEntity.ok(count);
    }

    /**
     * Check if enrollment exists by enrollment ID
     */
    @GetMapping("/{enrollmentId}/exists")
    public ResponseEntity<Boolean> enrollmentExists(@PathVariable String enrollmentId) {
        boolean exists = enrollmentJpaService.existsByEnrollmentId(enrollmentId);
        return ResponseEntity.ok(exists);
    }

    /**
     * Get enrollment statistics by subject for a school
     */
    @GetMapping("/school/{schoolCode}/stats/subject")
    public ResponseEntity<List<Object[]>> getEnrollmentStatsBySubject(@PathVariable String schoolCode) {
        List<Object[]> stats = enrollmentJpaService.getEnrollmentStatsBySubject(schoolCode);
        return ResponseEntity.ok(stats);
    }
}