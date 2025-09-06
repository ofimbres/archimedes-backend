package com.binomiaux.archimedes.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.exception.common.EntityNotFoundException;
import com.binomiaux.archimedes.model.School;
import com.binomiaux.archimedes.repository.SchoolRepository;

/**
 * Service layer for School operations.
 * Handles business logic and coordinates with the repository layer.
 */
@Service
public class SchoolService {

    private final SchoolRepository schoolRepository;

    public SchoolService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    /**
     * Get a school by its ID.
     * 
     * @param id the school identifier
     * @return the School entity
     * @throws EntityNotFoundException if school is not found
     */
    public School getSchool(String id) {
        School school = schoolRepository.findById(id);
        if (school == null) {
            throw new EntityNotFoundException("School with ID " + id + " not found", null);
        }
        return school;
    }

    /**
     * Get a school by its school code.
     * 
     * @param schoolCode the school code
     * @return the School entity
     * @throws EntityNotFoundException if school is not found
     */
    public School getSchoolByCode(String schoolCode) {
        School school = schoolRepository.findBySchoolCode(schoolCode);
        if (school == null) {
            throw new EntityNotFoundException("School with code " + schoolCode + " not found", null);
        }
        return school;
    }

    /**
     * Get all schools.
     * 
     * @return list of all schools
     */
    public List<School> getSchools() {
        return schoolRepository.findAll();
    }

    /**
     * Create a new school.
     * 
     * @param school the school to create
     * @return the created school
     */
    public School createSchool(School school) {
        // Business validation could go here
        // e.g., validate school code uniqueness, required fields, etc.
        
        schoolRepository.create(school);
        return school;
    }

    /**
     * Update an existing school.
     * 
     * @param id the school ID to update
     * @param updatedSchool the updated school data
     * @return the updated school
     * @throws EntityNotFoundException if school is not found
     */
    public School updateSchool(String id, School updatedSchool) {
        // Verify the school exists
        School existingSchool = getSchool(id);
        
        // Update fields while preserving keys
        updatedSchool.setId(existingSchool.getId());
        updatedSchool.setPk(existingSchool.getPk());
        updatedSchool.setSk(existingSchool.getSk());
        updatedSchool.setType(existingSchool.getType());
        
        schoolRepository.update(updatedSchool);
        return updatedSchool;
    }

    /**
     * Delete a school by ID.
     * 
     * @param id the school identifier
     * @throws EntityNotFoundException if school is not found
     */
    public void deleteSchool(String id) {
        // Verify the school exists first
        getSchool(id);
        
        // In a real scenario, you might want to check if there are
        // students or teachers still associated with this school
        schoolRepository.delete(id);
    }

    /**
     * Check if a school exists by ID.
     * 
     * @param id the school identifier
     * @return true if the school exists, false otherwise
     */
    public boolean schoolExists(String id) {
        return schoolRepository.findById(id) != null;
    }

    /**
     * Check if a school code is already in use.
     * 
     * @param schoolCode the school code to check
     * @return true if the code exists, false otherwise
     */
    public boolean schoolCodeExists(String schoolCode) {
        return schoolRepository.findBySchoolCode(schoolCode) != null;
    }
}
