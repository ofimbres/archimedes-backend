package com.binomiaux.archimedes.repository.api;

import com.binomiaux.archimedes.model.Teacher;

public interface TeacherRepository {
    Teacher find(String teacherId);
    void create(Teacher teacher);
}