package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.Period;

public interface PeriodService {
    Period getPeriod(String code);
    void create(Period period);
}
