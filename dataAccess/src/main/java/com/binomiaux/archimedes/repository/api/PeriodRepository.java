package com.binomiaux.archimedes.repository.api;

import java.util.List;

import com.binomiaux.archimedes.model.Period;

public interface PeriodRepository {
    void create(Period period);
    List<Period> getPeriodsByStudentId(String studentId);
}
