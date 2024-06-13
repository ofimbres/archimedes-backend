package com.binomiaux.archimedes.repository.converter;

import com.binomiaux.archimedes.repository.schema.StudentRecord;
import com.binomiaux.archimedes.model.Student;

public class StudentRecordTransform implements RecordTransform<StudentRecord, Student> {
    @Override
    public Student transform(StudentRecord entity) {
        Student model = new Student(entity.getUsername(), entity.getFirstName(), entity.getLastName(), entity.getEmail());
        return model;
    }

    @Override
    public StudentRecord untransform(Student model) {
        return null;
    }
}
