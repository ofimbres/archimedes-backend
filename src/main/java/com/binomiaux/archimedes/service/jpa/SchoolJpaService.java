package com.binomiaux.archimedes.service.jpa;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.binomiaux.archimedes.entity.School;
import com.binomiaux.archimedes.repository.jpa.SchoolRepository;

@Service("schoolJpaService")
@Transactional
public class SchoolJpaService {
    
    @Autowired
    private SchoolRepository schoolRepository;

    public School getSchoolByCode(String schoolCode) {
        return schoolRepository.findBySchoolCode(schoolCode)
            .orElseThrow(() -> new RuntimeException("School not found: " + schoolCode));
    }

    public School getSchoolById(String id) {
        return schoolRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("School not found with ID: " + id));
    }

    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }

    public List<School> getActiveSchools() {
        return schoolRepository.findAllActiveSchools();
    }

    public School createSchool(String id, String schoolCode, String name) {
        return createSchool(id, schoolCode, name, null, null, null, null);
    }

    public School createSchool(String id, String schoolCode, String name, String address, 
                              String principalName, String contactEmail, String phoneNumber) {
        if (schoolRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("School ID already exists: " + id);
        }
        
        if (schoolRepository.findBySchoolCode(schoolCode).isPresent()) {
            throw new IllegalArgumentException("School code already exists: " + schoolCode);
        }

        School school = new School();
        school.setId(id);
        school.setSchoolCode(schoolCode);
        school.setName(name);
        school.setAddress(address);
        school.setPrincipalName(principalName);
        school.setContactEmail(contactEmail);
        school.setPhoneNumber(phoneNumber);
        school.setStatus("ACTIVE");

        return schoolRepository.save(school);
    }

    public List<School> searchSchoolsByName(String name) {
        return schoolRepository.findByNameContainingIgnoreCase(name);
    }

    public long getStudentCount(String schoolId) {
        return schoolRepository.countActiveStudentsBySchool(schoolId);
    }
}
