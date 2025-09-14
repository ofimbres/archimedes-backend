package com.binomiaux.archimedes.dto.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.binomiaux.archimedes.dto.response.StudentResponse;
import com.binomiaux.archimedes.entity.Student;

/**
 * JPA version of StudentMapper to convert between JPA Student entity and DTOs
 */
public class StudentMapperJpa {

    /**
     * Convert JPA Student entity to StudentResponse DTO
     */
    public static StudentResponse toResponse(Student student) {
        if (student == null) {
            return null;
        }
        
        return new StudentResponse(
            student.getStudentId(),
            student.getSchool().getSchoolCode(),
            student.getFirstName(),
            student.getLastName(),
            student.getFullName(), // Use the method from the entity
            student.getEmail(),
            student.getUsername()
        );
    }

    /**
     * Convert list of JPA Student entities to list of StudentResponse DTOs
     */
    public static List<StudentResponse> toResponseList(List<Student> students) {
        if (students == null) {
            return null;
        }
        
        return students.stream()
                .map(StudentMapperJpa::toResponse)
                .collect(Collectors.toList());
    }
}