package com.binomiaux.archimedes.dto.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.binomiaux.archimedes.dto.response.TeacherResponse;
import com.binomiaux.archimedes.model.Teacher;

/**
 * Mapper for converting Teacher entities to TeacherResponse DTOs.
 */
public class TeacherMapper {

    private TeacherMapper() {
        // Utility class
    }

    /**
     * Convert Teacher entity to TeacherResponse DTO.
     */
    public static TeacherResponse toResponse(Teacher teacher) {
        if (teacher == null) {
            return null;
        }

        return new TeacherResponse(
                teacher.getTeacherId(),
                teacher.getUsername(),
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getFullName(),
                teacher.getEmail(),
                teacher.getSchoolId(),
                teacher.getMaxPeriods()
        );
    }

    /**
     * Convert list of Teacher entities to list of TeacherResponse DTOs.
     */
    public static List<TeacherResponse> toResponseList(List<Teacher> teachers) {
        if (teachers == null) {
            return null;
        }

        return teachers.stream()
                .map(TeacherMapper::toResponse)
                .collect(Collectors.toList());
    }
}
