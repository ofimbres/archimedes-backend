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
     * Get period by ID - used by other services
     */
    public Period getPeriod(String periodId) {
        return periodRepository.find(periodId);
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
}