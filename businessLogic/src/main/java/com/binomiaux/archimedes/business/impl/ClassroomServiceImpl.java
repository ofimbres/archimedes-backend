package com.binomiaux.archimedes.business.impl;

import com.binomiaux.archimedes.business.ClassroomService;
import com.binomiaux.archimedes.model.Classroom;
import org.springframework.stereotype.Service;

@Service
public class ClassroomServiceImpl implements ClassroomService {
    @Override
    public Classroom getClassroom(String id) {
        Classroom classroom = new Classroom();
        classroom.setId("e46e7191-e31d-434a-aba3-b9a9c187a632");
        classroom.setName("Math");

        return classroom;
    }
}
