package com.binomiaux.archimedes.dto.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.binomiaux.archimedes.dto.response.PeriodResponse;
import com.binomiaux.archimedes.entity.Period;

/**
 * JPA version of PeriodMapper to convert between JPA Period entity and DTOs
 */
public class PeriodMapperJpa {

    /**
     * Convert JPA Period entity to PeriodResponse DTO
     */
    public static PeriodResponse toResponse(Period period) {
        if (period == null) {
            return null;
        }
        
        PeriodResponse response = new PeriodResponse();
        response.setId(String.valueOf(period.getId())); // Convert Long to String
        response.setPeriodId(period.getPeriodId());
        response.setSchoolId(String.valueOf(period.getSchool().getId())); // Convert Long to String
        response.setTeacherId(period.getTeacher().getTeacherId()); // Use business ID
        response.setTeacherName(period.getTeacher().getFirstName() + " " + period.getTeacher().getLastName());
        response.setName(period.getName());
        response.setSubject(period.getSubject());
        response.setPeriodNumber(period.getPeriodNumber());
        response.setStartTime(period.getStartTime());
        response.setEndTime(period.getEndTime());
        response.setRoomNumber(period.getRoomNumber());
        response.setMaxStudents(period.getMaxStudents());
        response.setAcademicYear(period.getAcademicYear());
        response.setSemester(period.getSemester());
        response.setStatus(period.getStatus());
        response.setCreatedDate(period.getCreatedDate());
        
        // Default current enrollment to 0 - can be populated later if needed
        response.setCurrentEnrollment(0);
        
        return response;
    }

    /**
     * Convert list of JPA Period entities to list of PeriodResponse DTOs
     */
    public static List<PeriodResponse> toResponseList(List<Period> periods) {
        if (periods == null) {
            return null;
        }
        
        return periods.stream()
                .map(PeriodMapperJpa::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a minimal response with just basic period info
     */
    public static PeriodResponse toMinimalResponse(Period period) {
        if (period == null) {
            return null;
        }
        
        PeriodResponse response = new PeriodResponse();
        response.setId(String.valueOf(period.getId()));
        response.setPeriodId(period.getPeriodId());
        response.setName(period.getName());
        response.setSubject(period.getSubject());
        response.setPeriodNumber(period.getPeriodNumber());
        response.setTeacherId(period.getTeacher().getTeacherId());
        response.setTeacherName(period.getTeacher().getFirstName() + " " + period.getTeacher().getLastName());
        
        return response;
    }

    /**
     * Convert to response with enrollment count populated
     */
    public static PeriodResponse toResponseWithEnrollment(Period period, Integer enrollmentCount) {
        PeriodResponse response = toResponse(period);
        if (response != null && enrollmentCount != null) {
            response.setCurrentEnrollment(enrollmentCount);
        }
        return response;
    }
}