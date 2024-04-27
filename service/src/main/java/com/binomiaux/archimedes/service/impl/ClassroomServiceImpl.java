package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.ClassroomService;
import com.binomiaux.archimedes.model.Classroom;
import org.springframework.stereotype.Service;

@Service
public class ClassroomServiceImpl implements ClassroomService {
    @Override
    public Classroom getClassroom(String id) {
        Classroom classroom = new Classroom("e46e7191-e31d-434a-aba3-b9a9c187a632", "Math");

        return classroom;
    }
}
