package com.binomiaux.archimedes.repository.mapper;

import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.entities.StudentEntity;
import com.binomiaux.archimedes.repository.util.DynamoKeyBuilder;

/**
 * Simple mapper between Student domain model and StudentEntity.
 * Handles the conversion without over-engineering.
 */
public class StudentMapper {

    /**
     * Convert domain Student to StudentEntity for persistence.
     */
    public static StudentEntity toEntity(Student student) {
        if (student == null) {
            return null;
        }

        StudentEntity entity = new StudentEntity();
        
        // Set business fields
        entity.setStudentId(student.getStudentId());
        entity.setSchoolId(student.getSchoolId());
        entity.setFirstName(student.getFirstName());
        entity.setLastName(student.getLastName());
        entity.setUsername(student.getUsername());
        entity.setEmail(student.getEmail());
        
        // Set DynamoDB keys
        entity.setPk(DynamoKeyBuilder.buildStudentKey(student.getStudentId()));
        entity.setSk("STUDENT");
        entity.setType("STUDENT");
        
        // Set GSI keys for querying
        entity.setGsi1pk(DynamoKeyBuilder.buildSchoolKey(student.getSchoolId()));
        entity.setGsi1sk("STUDENT#" + student.getStudentId());
        
        if (student.getUsername() != null) {
            entity.setGsi2pk("USERNAME");
            entity.setGsi2sk(student.getUsername());
        }
        
        return entity;
    }

    /**
     * Convert StudentEntity to domain Student.
     */
    public static Student toDomain(StudentEntity entity) {
        if (entity == null) {
            return null;
        }

        Student student = new Student();
        student.setStudentId(entity.getStudentId());
        student.setSchoolId(entity.getSchoolId());
        student.setFirstName(entity.getFirstName());
        student.setLastName(entity.getLastName());
        student.setUsername(entity.getUsername());
        student.setEmail(entity.getEmail());
        
        return student;
    }
}
