package com.binomiaux.archimedes.controller.jpa;

import java.time.LocalTime;
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

import com.binomiaux.archimedes.dto.mapper.PeriodMapperJpa;
import com.binomiaux.archimedes.dto.response.PeriodResponse;
import com.binomiaux.archimedes.entity.Period;
import com.binomiaux.archimedes.service.jpa.PeriodJpaService;

/**
 * Pure JPA-based Period controller - completely separate from DynamoDB implementation.
 * Uses different endpoint path to avoid conflicts.
 */
@RestController
@RequestMapping("/api/v1/periods")
public class PeriodJpaController {

    private final PeriodJpaService periodJpaService;

    public PeriodJpaController(PeriodJpaService periodJpaService) {
        this.periodJpaService = periodJpaService;
    }

    /**
     * Get all periods
     */
    @GetMapping
    public ResponseEntity<List<PeriodResponse>> getAllPeriods() {
        List<Period> periods = periodJpaService.getAllPeriods();
        List<PeriodResponse> response = PeriodMapperJpa.toResponseList(periods);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a period by period ID
     */
    @GetMapping("/{periodId}")
    public ResponseEntity<PeriodResponse> getPeriod(@PathVariable String periodId) {
        Period period = periodJpaService.getPeriodByPeriodId(periodId);
        PeriodResponse response = PeriodMapperJpa.toResponse(period);
        return ResponseEntity.ok(response);
    }

    /**
     * Get periods by school code
     */
    @GetMapping("/school/{schoolCode}")
    public ResponseEntity<List<PeriodResponse>> getPeriodsBySchool(@PathVariable String schoolCode) {
        List<Period> periods = periodJpaService.getPeriodsBySchoolCode(schoolCode);
        List<PeriodResponse> response = PeriodMapperJpa.toResponseList(periods);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active periods by school code
     */
    @GetMapping("/school/{schoolCode}/active")
    public ResponseEntity<List<PeriodResponse>> getActivePeriodsBySchool(@PathVariable String schoolCode) {
        List<Period> periods = periodJpaService.getActivePeriodsOrderedByNumber(schoolCode);
        List<PeriodResponse> response = PeriodMapperJpa.toResponseList(periods);
        return ResponseEntity.ok(response);
    }

    /**
     * Get periods by teacher ID
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<PeriodResponse>> getPeriodsByTeacher(@PathVariable String teacherId) {
        List<Period> periods = periodJpaService.getPeriodsByTeacherId(teacherId);
        List<PeriodResponse> response = PeriodMapperJpa.toResponseList(periods);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active periods by teacher ID
     */
    @GetMapping("/teacher/{teacherId}/active")
    public ResponseEntity<List<PeriodResponse>> getActivePeriodsByTeacher(@PathVariable String teacherId) {
        List<Period> periods = periodJpaService.getActivePeriodsByTeacherId(teacherId);
        List<PeriodResponse> response = PeriodMapperJpa.toResponseList(periods);
        return ResponseEntity.ok(response);
    }

    /**
     * Get periods by school and subject
     */
    @GetMapping("/school/{schoolCode}/subject/{subject}")
    public ResponseEntity<List<PeriodResponse>> getPeriodsBySubject(
            @PathVariable String schoolCode,
            @PathVariable String subject) {
        List<Period> periods = periodJpaService.getPeriodsBySchoolAndSubject(schoolCode, subject);
        List<PeriodResponse> response = PeriodMapperJpa.toResponseList(periods);
        return ResponseEntity.ok(response);
    }

    /**
     * Get periods by school and period number
     */
    @GetMapping("/school/{schoolCode}/number/{periodNumber}")
    public ResponseEntity<List<PeriodResponse>> getPeriodsByNumber(
            @PathVariable String schoolCode,
            @PathVariable Integer periodNumber) {
        List<Period> periods = periodJpaService.getPeriodsBySchoolAndNumber(schoolCode, periodNumber);
        List<PeriodResponse> response = PeriodMapperJpa.toResponseList(periods);
        return ResponseEntity.ok(response);
    }

    /**
     * Get periods by academic year
     */
    @GetMapping("/academic-year/{academicYear}")
    public ResponseEntity<List<PeriodResponse>> getPeriodsByAcademicYear(
            @RequestParam String schoolCode,
            @PathVariable String academicYear) {
        List<Period> periods = periodJpaService.getPeriodsByAcademicYear(schoolCode, academicYear);
        List<PeriodResponse> response = PeriodMapperJpa.toResponseList(periods);
        return ResponseEntity.ok(response);
    }

    /**
     * Get periods by semester
     */
    @GetMapping("/semester/{semester}")
    public ResponseEntity<List<PeriodResponse>> getPeriodsBySemester(
            @RequestParam String schoolCode,
            @PathVariable String semester) {
        List<Period> periods = periodJpaService.getPeriodsBySemester(schoolCode, semester);
        List<PeriodResponse> response = PeriodMapperJpa.toResponseList(periods);
        return ResponseEntity.ok(response);
    }

    /**
     * Get periods by student ID
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PeriodResponse>> getPeriodsByStudent(@PathVariable Long studentId) {
        List<Period> periods = periodJpaService.getPeriodsByStudentId(studentId);
        List<PeriodResponse> response = PeriodMapperJpa.toResponseList(periods);
        return ResponseEntity.ok(response);
    }

    /**
     * Get subjects by school
     */
    @GetMapping("/school/{schoolCode}/subjects")
    public ResponseEntity<List<String>> getSubjectsBySchool(@PathVariable String schoolCode) {
        List<String> subjects = periodJpaService.getSubjectsBySchool(schoolCode);
        return ResponseEntity.ok(subjects);
    }

    /**
     * Create a new period
     */
    @PostMapping
    public ResponseEntity<PeriodResponse> createPeriod(
            @RequestParam String schoolCode,
            @RequestParam String teacherId,
            @RequestParam String name,
            @RequestParam String subject,
            @RequestParam Integer periodNumber,
            @RequestParam(required = false) String startTimeStr,
            @RequestParam(required = false) String endTimeStr,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) Integer maxStudents,
            @RequestParam String academicYear,
            @RequestParam String semester) {
        
        LocalTime startTime = startTimeStr != null ? LocalTime.parse(startTimeStr) : null;
        LocalTime endTime = endTimeStr != null ? LocalTime.parse(endTimeStr) : null;
        
        Period period = periodJpaService.createPeriod(schoolCode, teacherId, name, subject, 
                                                    periodNumber, startTime, endTime, roomNumber, 
                                                    maxStudents, academicYear, semester);
        PeriodResponse response = PeriodMapperJpa.toResponse(period);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a period
     */
    @PutMapping("/{periodId}")
    public ResponseEntity<PeriodResponse> updatePeriod(
            @PathVariable String periodId,
            @RequestParam String name,
            @RequestParam String subject,
            @RequestParam Integer periodNumber,
            @RequestParam(required = false) String startTimeStr,
            @RequestParam(required = false) String endTimeStr,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) Integer maxStudents) {
        
        LocalTime startTime = startTimeStr != null ? LocalTime.parse(startTimeStr) : null;
        LocalTime endTime = endTimeStr != null ? LocalTime.parse(endTimeStr) : null;
        
        Period period = periodJpaService.updatePeriod(periodId, name, subject, periodNumber,
                                                    startTime, endTime, roomNumber, maxStudents);
        PeriodResponse response = PeriodMapperJpa.toResponse(period);
        return ResponseEntity.ok(response);
    }

    /**
     * Update period teacher
     */
    @PutMapping("/{periodId}/teacher")
    public ResponseEntity<PeriodResponse> updatePeriodTeacher(
            @PathVariable String periodId,
            @RequestParam String teacherId) {
        Period period = periodJpaService.updatePeriodTeacher(periodId, teacherId);
        PeriodResponse response = PeriodMapperJpa.toResponse(period);
        return ResponseEntity.ok(response);
    }

    /**
     * Update period capacity
     */
    @PutMapping("/{periodId}/capacity")
    public ResponseEntity<PeriodResponse> updatePeriodCapacity(
            @PathVariable String periodId,
            @RequestParam Integer maxStudents) {
        Period period = periodJpaService.updatePeriodCapacity(periodId, maxStudents);
        PeriodResponse response = PeriodMapperJpa.toResponse(period);
        return ResponseEntity.ok(response);
    }

    /**
     * Activate a period
     */
    @PutMapping("/{periodId}/activate")
    public ResponseEntity<PeriodResponse> activatePeriod(@PathVariable String periodId) {
        Period period = periodJpaService.activatePeriod(periodId);
        PeriodResponse response = PeriodMapperJpa.toResponse(period);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a period (soft delete)
     */
    @PutMapping("/{periodId}/deactivate")
    public ResponseEntity<Void> deactivatePeriod(@PathVariable String periodId) {
        periodJpaService.deactivatePeriod(periodId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a period permanently
     */
    @DeleteMapping("/{periodId}")
    public ResponseEntity<Void> deletePeriod(@PathVariable String periodId) {
        periodJpaService.deletePeriod(periodId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get active enrollment count for a period
     */
    @GetMapping("/{periodId}/enrollment/count")
    public ResponseEntity<Long> getEnrollmentCount(@PathVariable String periodId) {
        long count = periodJpaService.getActiveEnrollmentCount(periodId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get period count by school
     */
    @GetMapping("/school/{schoolCode}/count")
    public ResponseEntity<Long> getPeriodCount(@PathVariable String schoolCode) {
        long count = periodJpaService.getPeriodCountBySchool(schoolCode);
        return ResponseEntity.ok(count);
    }

    /**
     * Check if period exists by period ID
     */
    @GetMapping("/{periodId}/exists")
    public ResponseEntity<Boolean> periodExists(@PathVariable String periodId) {
        boolean exists = periodJpaService.existsByPeriodId(periodId);
        return ResponseEntity.ok(exists);
    }
}