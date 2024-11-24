package com.binomiaux.archimedes.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.entities.StudentEntity;

@Mapper
public interface StudentMapper {
    StudentMapper INSTANCE = Mappers.getMapper( StudentMapper.class );
 
    //@Mapping(source = "numberOfSeats", target = "seatCount")
    StudentEntity studentToEntity(Student model);
    Student entityToStudent(StudentEntity entity);
}
