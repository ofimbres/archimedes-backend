package com.binomiaux.archimedes.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.binomiaux.archimedes.model.ActivityResult;
import com.binomiaux.archimedes.repository.entities.ActivityResultEntity;

@Mapper
public interface ActivityResultMapper {
    ActivityResultMapper INSTANCE = Mappers.getMapper( ActivityResultMapper.class );
 
    //@Mapping(source = "numberOfSeats", target = "seatCount")
    ActivityResultEntity activityResultToEntity(ActivityResult entity);
    ActivityResult entityToActivityResult(ActivityResultEntity model);
}

