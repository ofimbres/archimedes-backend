package com.binomiaux.archimedes.repository.api;

import java.util.List;

import com.binomiaux.archimedes.model.Period;

public interface PeriodRepository {
    Period find(String periodId);
    void create(Period period);
    List<Period> getPeriodsByStudent(String studentId);
    List<Period> getPeriodsByTeacher(String teacherId);
}
