package com.binomiaux.archimedes.repository.transform;

import com.binomiaux.archimedes.repository.schema.StudentRecord;
import com.binomiaux.archimedes.model.Student;

public class StudentRecordTransform implements RecordTransform<StudentRecord, Student> {
    @Override
    public Student transform(StudentRecord entity) {
        Student model = new Student();
        model.setId(entity.getUsername());
        model.setFirstName(entity.getFirstName());
        model.setLastName(entity.getLastName());
        model.setEmail(entity.getEmail());
        return model;
    }

    @Override
    public StudentRecord untransform(Student model) {
        return null;
    }
}
