package com.binomiaux.archimedes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.model.School;
import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.repository.TeacherRepository;
import com.binomiaux.archimedes.exception.common.EntityNotFoundException;
import com.binomiaux.archimedes.service.TeacherService;

@Service
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private SchoolService schoolService;

    public void createTeacher(Teacher teacher) {
        // Business logic validation - check if school exists
        School school = schoolService.getSchool(teacher.getSchoolId());
        if (school == null) {
            throw new EntityNotFoundException("School " + teacher.getSchoolId() + " not found", null);
        }
        
        teacherRepository.create(teacher);
    }

    public Teacher getTeacher(String id) {
        Teacher teacher = teacherRepository.find(id);
        if (teacher == null) {
            throw new EntityNotFoundException("Teacher " + id + " not found", null);
        }
        return teacher;
    }
}
