package com.binomiaux.archimedes.repository.converter;

import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.repository.entities.TeacherEntity;

public class TeacherEntityTransform implements EntityTransform<TeacherEntity, Teacher> {
    @Override
    public Teacher transform(TeacherEntity entity) {
        //var model = new Teacher(entity.getTeacherCode(), entity.getFirstName(), entity.getLastName(), entity.getEmail(), entity.getSchoolCode(), entity.getUsername());
        return null;
    }

    @Override
    public TeacherEntity untransform(Teacher model) {
        //var entity = new TeacherEntity("TEACHER#" + model.getTeacherCode(), "STUDENT#" + model.getTeacherCode(), "TEACHER", model.getTeacherCode(), model.getFirstName(), model.getLastName(), model.getUsername(), model.getEmail(), model.getSchoolCode());
        return null;
    }
}
