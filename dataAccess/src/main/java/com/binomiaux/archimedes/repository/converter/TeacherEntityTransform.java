package com.binomiaux.archimedes.repository.converter;

import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.repository.entities.TeacherEntity;

public class TeacherEntityTransform implements EntityTransform<TeacherEntity, Teacher> {
    @Override
    public Teacher transform(TeacherEntity entity) {
        var model = new Teacher(entity.getCode(), entity.getFirstName(), entity.getLastName(), entity.getEmail(), entity.getSchoolCode(), entity.getUsername());
        return model;
    }

    @Override
    public TeacherEntity untransform(Teacher model) {
        var entity = new TeacherEntity("STUDENT#" + model.getCode(), "STUDENT#" + model.getCode(), "TEACHER", model.getCode(), model.getFirstName(), model.getLastName(), model.getUsername(), model.getEmail(), model.getSchoolCode());
        return entity;
    }
}
