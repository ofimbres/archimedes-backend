package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.PeriodService;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.repository.api.PeriodRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PeriodServiceImpl implements PeriodService {

    @Autowired
    private PeriodRepository periodRepository;

    @Override
    public Period getPeriod(String code) {
        return null;
    }

    @Override
    public void create(Period period) {
        periodRepository.create(period);
    }

    public List<Period> getPeriodsByTeacher(String teacherId) {
        return periodRepository.getPeriodsByTeacher(teacherId);
    }
}