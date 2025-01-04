package com.binomiaux.archimedes.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.repository.api.TeacherRepository;
import com.binomiaux.archimedes.service.TeacherService;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Override
    public void createTeacher(Teacher teacher) {
        teacherRepository.create(teacher);
    }

    @Override
    public Teacher getTeacher(String id) {
        return teacherRepository.find(id);
    }
}
