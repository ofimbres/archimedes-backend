package com.binomiaux.archimedes.repository.converter;

import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.entities.StudentEntity;

public class StudentEntityTransform implements EntityTransform<StudentEntity, Student> {
    @Override
    public Student transform(StudentEntity entity) {
        Student model = new Student(entity.getUsername(), entity.getFirstName(), entity.getLastName(), entity.getEmail(), "", "");
        return model;
    }

    @Override
    public StudentEntity untransform(Student model) {
        return null;
    }
}
