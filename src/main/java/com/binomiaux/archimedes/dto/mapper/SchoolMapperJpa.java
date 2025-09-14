package com.binomiaux.archimedes.dto.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.binomiaux.archimedes.dto.response.SchoolResponse;
import com.binomiaux.archimedes.entity.School;

/**
 * JPA version of SchoolMapper to convert between JPA School entity and DTOs
 */
public class SchoolMapperJpa {

    /**
     * Convert JPA School entity to SchoolResponse DTO
     */
    public static SchoolResponse toResponse(School school) {
        if (school == null) {
            return null;
        }
        
        SchoolResponse response = new SchoolResponse();
        response.setId(String.valueOf(school.getId())); // Convert Long to String
        response.setSchoolCode(school.getSchoolCode());
        response.setName(school.getName());
        response.setAddress(school.getAddress());
        response.setPrincipalName(school.getPrincipalName());
        response.setContactEmail(school.getContactEmail());
        response.setPhoneNumber(school.getPhoneNumber());
        
        // For now, set counts to 0 - we can implement these later if needed
        response.setStudentCount(0);
        response.setTeacherCount(0);
        response.setPeriodCount(0);
        
        return response;
    }

    /**
     * Convert list of JPA School entities to list of SchoolResponse DTOs
     */
    public static List<SchoolResponse> toResponseList(List<School> schools) {
        if (schools == null) {
            return null;
        }
        
        return schools.stream()
                .map(SchoolMapperJpa::toResponse)
                .collect(Collectors.toList());
    }
}