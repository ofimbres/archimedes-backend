package com.binomiaux.archimedes.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.dto.request.CreateSchoolRequest;
import com.binomiaux.archimedes.dto.response.SchoolResponse;
import com.binomiaux.archimedes.model.School;
import com.binomiaux.archimedes.service.SchoolService;

/**
 * School controller handling CRUD operations for schools.
 * 
 * Provides RESTful endpoints for:
 * - School retrieval (by ID, by code, all schools)
 * - School creation
 * - School updates
 * - School deletion
 */
@RestController
@RequestMapping("/api/v1/schools")
public class SchoolController {

    private final SchoolService schoolService;

    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    /**
     * Get all schools.
     * 
     * @return list of all schools
     */
    @GetMapping
    public ResponseEntity<List<SchoolResponse>> getAllSchools() {
        List<School> schools = schoolService.getSchools();
        List<SchoolResponse> response = schools.stream()
                .map(SchoolResponse::fromSchool)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get a school by its ID.
     * 
     * @param schoolId the school identifier
     * @return the school entity
     */
    @GetMapping("/{schoolId}")
    public ResponseEntity<SchoolResponse> getSchool(@PathVariable String schoolId) {
        School school = schoolService.getSchool(schoolId);
        SchoolResponse response = SchoolResponse.fromSchool(school);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a school by its school code.
     * 
     * @param schoolCode the school code
     * @return the school entity
     */
    @GetMapping("/code/{schoolCode}")
    public ResponseEntity<SchoolResponse> getSchoolByCode(@PathVariable String schoolCode) {
        School school = schoolService.getSchoolByCode(schoolCode);
        SchoolResponse response = SchoolResponse.fromSchool(school);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new school.
     * 
     * @param request the school creation request
     * @return the created school
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<SchoolResponse> createSchool(@RequestBody CreateSchoolRequest request) {
        // Convert DTO to entity
        School school = new School(request.getId(), request.getSchoolCode(), request.getName());
        school.setAddress(request.getAddress());
        school.setPrincipalName(request.getPrincipalName());
        school.setContactEmail(request.getContactEmail());
        school.setPhoneNumber(request.getPhoneNumber());
        
        School createdSchool = schoolService.createSchool(school);
        SchoolResponse response = SchoolResponse.fromSchool(createdSchool);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing school.
     * 
     * @param schoolId the school ID to update
     * @param request the updated school data
     * @return the updated school
     */
    @PutMapping("/{schoolId}")
    public ResponseEntity<SchoolResponse> updateSchool(@PathVariable String schoolId, 
                                                     @RequestBody CreateSchoolRequest request) {
        // Convert DTO to entity
        School school = new School(request.getId(), request.getSchoolCode(), request.getName());
        school.setAddress(request.getAddress());
        school.setPrincipalName(request.getPrincipalName());
        school.setContactEmail(request.getContactEmail());
        school.setPhoneNumber(request.getPhoneNumber());
        
        School updatedSchool = schoolService.updateSchool(schoolId, school);
        SchoolResponse response = SchoolResponse.fromSchool(updatedSchool);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a school by ID.
     * 
     * @param schoolId the school identifier
     */
    @DeleteMapping("/{schoolId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteSchool(@PathVariable String schoolId) {
        schoolService.deleteSchool(schoolId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if a school exists by ID.
     * 
     * @param schoolId the school identifier
     * @return true if school exists, false otherwise
     */
    @GetMapping("/{schoolId}/exists")
    public ResponseEntity<Boolean> schoolExists(@PathVariable String schoolId) {
        boolean exists = schoolService.schoolExists(schoolId);
        return ResponseEntity.ok(exists);
    }
}
