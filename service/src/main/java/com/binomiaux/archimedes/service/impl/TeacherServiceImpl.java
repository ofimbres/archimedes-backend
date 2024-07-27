package com.binomiaux.archimedes.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.repository.api.TeacherRepository;
import com.binomiaux.archimedes.repository.api.PeriodRepository;
import com.binomiaux.archimedes.repository.api.StudentRepository;
import com.binomiaux.archimedes.service.TeacherService;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private PeriodRepository periodRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public void create(Teacher teacher) {
        teacherRepository.create(teacher);
    }

    @Override
    public List<Student> getStudentsByPeriod(String periodId) {
        return studentRepository.getStudentsByPeriod(periodId);
    }

    @Override
    public List<Period> getPeriodsByTeacher(String teacherId) {
        return periodRepository.getPeriodsByTeacher(teacherId);
    }
}
