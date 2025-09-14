package com.binomiaux.archimedes.dto.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.binomiaux.archimedes.dto.response.EnrollmentResponse;
import com.binomiaux.archimedes.entity.Enrollment;

/**
 * JPA version of EnrollmentMapper to convert between JPA Enrollment entity and DTOs
 */
public class EnrollmentMapperJpa {

    /**
     * Convert JPA Enrollment entity to EnrollmentResponse DTO
     */
    public static EnrollmentResponse toResponse(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(String.valueOf(enrollment.getId())); // Convert Long to String
        response.setEnrollmentId(enrollment.getEnrollmentId());
        
        // Student information
        if (enrollment.getStudent() != null) {
            response.setStudentId(enrollment.getStudent().getStudentId());
            response.setStudentName(enrollment.getStudent().getFullName());
            response.setSchoolId(String.valueOf(enrollment.getStudent().getSchool().getId()));
        }
        
        // Period information
        if (enrollment.getPeriod() != null) {
            response.setPeriodId(enrollment.getPeriod().getPeriodId());
            response.setPeriodName(enrollment.getPeriod().getName());
            response.setSubject(enrollment.getPeriod().getSubject());
            
            // Teacher information
            if (enrollment.getPeriod().getTeacher() != null) {
                response.setTeacherId(enrollment.getPeriod().getTeacher().getTeacherId());
                response.setTeacherName(enrollment.getPeriod().getTeacher().getFirstName() + " " + 
                                       enrollment.getPeriod().getTeacher().getLastName());
            }
        }
        
        response.setEnrollmentDate(enrollment.getEnrollmentDate());
        response.setStatus(enrollment.getStatus());
        response.setGrade(enrollment.getGrade());
        response.setCreatedDate(enrollment.getCreatedDate());
        
        return response;
    }

    /**
     * Convert list of JPA Enrollment entities to list of EnrollmentResponse DTOs
     */
    public static List<EnrollmentResponse> toResponseList(List<Enrollment> enrollments) {
        if (enrollments == null) {
            return null;
        }
        
        return enrollments.stream()
                .map(EnrollmentMapperJpa::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a minimal response with just basic enrollment info
     */
    public static EnrollmentResponse toMinimalResponse(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(String.valueOf(enrollment.getId()));
        response.setEnrollmentId(enrollment.getEnrollmentId());
        response.setStudentName(enrollment.getStudentFullName());
        response.setPeriodName(enrollment.getPeriodDisplayName());
        response.setStatus(enrollment.getStatus());
        
        return response;
    }

    /**
     * Create a student-focused response (for student's schedule view)
     */
    public static EnrollmentResponse toStudentResponse(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(String.valueOf(enrollment.getId()));
        response.setEnrollmentId(enrollment.getEnrollmentId());
        
        if (enrollment.getPeriod() != null) {
            response.setPeriodId(enrollment.getPeriod().getPeriodId());
            response.setPeriodName(enrollment.getPeriod().getName());
            response.setSubject(enrollment.getPeriod().getSubject());
            
            if (enrollment.getPeriod().getTeacher() != null) {
                response.setTeacherName(enrollment.getPeriod().getTeacher().getFirstName() + " " + 
                                       enrollment.getPeriod().getTeacher().getLastName());
            }
        }
        
        response.setGrade(enrollment.getGrade());
        response.setStatus(enrollment.getStatus());
        
        return response;
    }

    /**
     * Create a period-focused response (for class roster view)
     */
    public static EnrollmentResponse toPeriodResponse(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(String.valueOf(enrollment.getId()));
        response.setEnrollmentId(enrollment.getEnrollmentId());
        
        if (enrollment.getStudent() != null) {
            response.setStudentId(enrollment.getStudent().getStudentId());
            response.setStudentName(enrollment.getStudent().getFullName());
        }
        
        response.setEnrollmentDate(enrollment.getEnrollmentDate());
        response.setGrade(enrollment.getGrade());
        response.setStatus(enrollment.getStatus());
        
        return response;
    }
}