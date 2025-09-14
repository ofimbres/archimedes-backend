package com.binomiaux.archimedes.controller.jpa;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.dto.mapper.SchoolMapperJpa;
import com.binomiaux.archimedes.dto.request.CreateSchoolRequest;
import com.binomiaux.archimedes.dto.response.SchoolResponse;
import com.binomiaux.archimedes.entity.School;
import com.binomiaux.archimedes.service.jpa.SchoolJpaService;

import jakarta.validation.Valid;

/**
 * Pure JPA-based School controller - completely separate from DynamoDB implementation.
 * Uses different endpoint path to avoid conflicts.
 */
@RestController
@RequestMapping("/api/v1/schools")
public class SchoolJpaController {

    private final SchoolJpaService schoolJpaService;

    public SchoolJpaController(SchoolJpaService schoolJpaService) {
        this.schoolJpaService = schoolJpaService;
    }

    /**
     * Get all schools
     */
    @GetMapping
    public ResponseEntity<List<SchoolResponse>> getAllSchools() {
        List<School> schools = schoolJpaService.getAllSchools();
        List<SchoolResponse> response = SchoolMapperJpa.toResponseList(schools);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all active schools
     */
    @GetMapping("/active")
    public ResponseEntity<List<SchoolResponse>> getActiveSchools() {
        List<School> schools = schoolJpaService.getActiveSchools();
        List<SchoolResponse> response = SchoolMapperJpa.toResponseList(schools);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a school by its code
     */
    @GetMapping("/{schoolCode}")
    public ResponseEntity<SchoolResponse> getSchoolByCode(@PathVariable String schoolCode) {
        School school = schoolJpaService.getSchoolByCode(schoolCode);
        SchoolResponse response = SchoolMapperJpa.toResponse(school);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new school
     */
    @PostMapping
    public ResponseEntity<SchoolResponse> createSchool(@Valid @RequestBody CreateSchoolRequest request) {
        School school = schoolJpaService.createSchool(
            request.getId(),
            request.getSchoolCode(), 
            request.getName(),
            request.getAddress(),
            request.getPrincipalName(),
            request.getContactEmail(),
            request.getPhoneNumber()
        );
        
        SchoolResponse response = SchoolMapperJpa.toResponse(school);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Search schools by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<SchoolResponse>> searchSchools(@RequestParam String name) {
        List<School> schools = schoolJpaService.searchSchoolsByName(name);
        List<SchoolResponse> response = SchoolMapperJpa.toResponseList(schools);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active student count for a school
     */
    @GetMapping("/{schoolCode}/student-count")
    public ResponseEntity<Long> getStudentCount(@PathVariable String schoolCode) {
        long count = schoolJpaService.getStudentCount(schoolCode);
        return ResponseEntity.ok(count);
    }
}