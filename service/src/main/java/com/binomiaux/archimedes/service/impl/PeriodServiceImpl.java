package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.PeriodService;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.repository.api.PeriodRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PeriodServiceImpl implements PeriodService {

    @Autowired
    private PeriodRepository periodRepository;

    @Override
    public Period getPeriod(String code) {
        Period classroom = new Period();
        classroom.setCode("e46e7191-e31d-434a-aba3-b9a9c187a632");
        classroom.setName("Math");

        return classroom;
    }

    @Override
    public void create(Period period) {
        periodRepository.create(period);
    }
}
