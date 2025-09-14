package com.binomiaux.archimedes.dto.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.binomiaux.archimedes.dto.response.TeacherResponse;
import com.binomiaux.archimedes.entity.Teacher;

/**
 * JPA version of TeacherMapper to convert between JPA Teacher entity and DTOs
 */
public class TeacherMapperJpa {

    /**
     * Convert JPA Teacher entity to TeacherResponse DTO
     */
    public static TeacherResponse toResponse(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        
        TeacherResponse response = new TeacherResponse();
        response.setTeacherId(teacher.getTeacherId());
        response.setFirstName(teacher.getFirstName());
        response.setLastName(teacher.getLastName());
        response.setFullName(teacher.getFirstName() + " " + teacher.getLastName());
        response.setEmail(teacher.getEmail());
        response.setSchoolId(String.valueOf(teacher.getSchool().getId())); // Convert Long to String
        
        // Set defaults for fields not in JPA entity
        response.setUsername(teacher.getEmail()); // Use email as username for now
        response.setMaxPeriods(5); // Default max periods
        
        return response;
    }

    /**
     * Convert list of JPA Teacher entities to list of TeacherResponse DTOs
     */
    public static List<TeacherResponse> toResponseList(List<Teacher> teachers) {
        if (teachers == null) {
            return null;
        }
        
        return teachers.stream()
                .map(TeacherMapperJpa::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a minimal response with just basic teacher info
     */
    public static TeacherResponse toMinimalResponse(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        
        TeacherResponse response = new TeacherResponse();
        response.setTeacherId(teacher.getTeacherId());
        response.setFirstName(teacher.getFirstName());
        response.setLastName(teacher.getLastName());
        response.setFullName(teacher.getFirstName() + " " + teacher.getLastName());
        response.setEmail(teacher.getEmail());
        
        return response;
    }
}