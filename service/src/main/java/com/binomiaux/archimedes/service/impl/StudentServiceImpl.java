package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.StudentService;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.api.PeriodRepository;
import com.binomiaux.archimedes.repository.api.StudentRepository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PeriodRepository periodRepository;

    @Override
    public Student getStudent(String id) {
        return studentRepository.find(id);
    }

    @Override
    public void create(Student student) {
        studentRepository.create(student);
    }

    @Override
    public boolean enrollStudentInPeriod(String studentId, String periodId) {
        List<Period> periods = periodRepository.getPeriodsByStudentId(studentId);
        boolean alreadyEnrolled = periods.stream().anyMatch(period -> period.getId().equals(periodId));

        if (alreadyEnrolled) {
            return true;
        }

        List<String> periodIds = periods.stream().map(Period::getId).collect(Collectors.toList());
        periodIds.add(periodId);
        studentRepository.updateStudentPeriods(studentId, periodIds);

        return true;
    }
}
