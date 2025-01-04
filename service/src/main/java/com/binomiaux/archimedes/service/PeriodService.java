package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.Period;

import java.util.List;

public interface PeriodService {
    Period getPeriodById(String code);
    void createPeriod(Period period);
    List<Period> getPeriodsByTeacherId(String teacherId);
    List<Period> getPeriodsByStudentId(String studentId);
}