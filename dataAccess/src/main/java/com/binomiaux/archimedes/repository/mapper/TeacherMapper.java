package com.binomiaux.archimedes.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.repository.entities.TeacherEntity;

@Mapper
public interface TeacherMapper {
    TeacherMapper INSTANCE = Mappers.getMapper( TeacherMapper.class );
 
    TeacherEntity teacherToEntity(Teacher model);
    Teacher entityToTeacher(TeacherEntity entity);
}