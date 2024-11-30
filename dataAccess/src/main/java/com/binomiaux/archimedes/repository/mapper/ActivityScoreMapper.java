package com.binomiaux.archimedes.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.binomiaux.archimedes.model.ActivityScore;
import com.binomiaux.archimedes.repository.entities.ActivityScoreEntity;

@Mapper
public interface ActivityScoreMapper {
    ActivityScoreMapper INSTANCE = Mappers.getMapper( ActivityScoreMapper.class );
 
    @Mapping(source = "student.studentId", target = "studentId")
    @Mapping(source = "student.firstName", target = "studentFirstName")
    @Mapping(source = "student.lastName", target = "studentLastName")
    @Mapping(source = "activity.activityId", target = "activityId")
    ActivityScoreEntity activityResultToEntity(ActivityScore entity);
    
    @Mapping(source = "studentId", target = "student.studentId")
    @Mapping(source = "studentFirstName", target = "student.firstName")
    @Mapping(source = "studentLastName", target = "student.lastName")
    @Mapping(source = "activityId", target = "activity.activityId")
    ActivityScore entityToActivityResult(ActivityScoreEntity model);
}

