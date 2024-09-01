package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.Period;

import java.util.List;

public interface PeriodService {
    Period getPeriod(String code);
    void create(Period period);
    List<Period> getPeriodsByTeacher(String teacherId);
}