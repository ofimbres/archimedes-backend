package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.repository.PeriodRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PeriodService {

    @Autowired
    private PeriodRepository periodRepository;

    /**
     * Get period by composite ID - used by other services
     */
    public Period getPeriod(String compositePeriodId) {
        return periodRepository.find(compositePeriodId);
    }

    /**
     * Get period by school, teacher and period IDs - alternative method
     */
    public Period getPeriod(String schoolId, String teacherId, String periodId) {
        String compositePeriodId = schoolId + "#" + teacherId + "#" + periodId;
        return periodRepository.find(compositePeriodId);
    }

    /**
     * Find period by school and period ID (searches across all teachers in school).
     * Used for registration codes where teacher ID is not available.
     */
    public Period findPeriodInSchool(String schoolId, String periodId) {
        return periodRepository.findPeriodInSchool(schoolId, periodId);
    }

    public void createPeriod(Period period) {
        periodRepository.create(period);
    }

    public List<Period> getPeriodsByTeacherId(String teacherId) {
        return periodRepository.getPeriodsByTeacher(teacherId);
    }

    public List<Period> getPeriodsByStudentId(String studentId) {
        return periodRepository.getPeriodsByStudent(studentId);
    }

    /**
     * Get all periods in a school - for school admin view
     */
    public List<Period> getPeriodsBySchool(String schoolId) {
        return periodRepository.getPeriodsBySchool(schoolId);
    }

    /**
     * Get periods by teacher within a specific school - maintains teacher-period relationship
     */
    public List<Period> getPeriodsByTeacherInSchool(String schoolId, String teacherId) {
        return periodRepository.getPeriodsByTeacherInSchool(schoolId, teacherId);
    }

    /**
     * Get periods by student within a specific school - for student dashboard
     */
    public List<Period> getPeriodsByStudentInSchool(String schoolId, String studentId) {
        return periodRepository.getPeriodsByStudentInSchool(schoolId, studentId);
    }
}