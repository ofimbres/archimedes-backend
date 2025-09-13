package com.binomiaux.archimedes.dto.mapper;

import com.binomiaux.archimedes.dto.response.StudentResponse;
import com.binomiaux.archimedes.model.Student;

/**
 * Mapper utility to convert between Student entity and DTOs
 */
public class StudentMapper {

    /**
     * Convert Student entity to StudentResponse DTO
     * Hides internal DynamoDB fields from API responses
     */
    public static StudentResponse toResponse(Student student) {
        if (student == null) {
            return null;
        }
        
        return new StudentResponse(
            student.getStudentId(),
            student.getSchoolId(),
            student.getFirstName(),
            student.getLastName(),
            student.getFullName(),
            student.getEmail(),
            student.getUsername()
        );
    }

    /**
     * Convert list of Student entities to list of StudentResponse DTOs
     */
    public static java.util.List<StudentResponse> toResponseList(java.util.List<Student> students) {
        if (students == null) {
            return null;
        }
        
        return students.stream()
                .map(StudentMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }
}
