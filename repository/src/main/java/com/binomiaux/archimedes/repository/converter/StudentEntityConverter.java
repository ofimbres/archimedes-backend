package com.binomiaux.archimedes.repository.converter;

import com.binomiaux.archimedes.model.dynamodb.StudentEntity;
import com.binomiaux.archimedes.model.pojo.Student;

public class StudentEntityConverter implements EntityConverter<StudentEntity, Student> {
    @Override
    public Student transform(StudentEntity entity) {
        Student model = new Student(entity.getUsername(), entity.getFirstName(), entity.getLastName(), entity.getEmail());
        return model;
    }

    @Override
    public StudentEntity untransform(Student model) {
        return null;
    }
}
