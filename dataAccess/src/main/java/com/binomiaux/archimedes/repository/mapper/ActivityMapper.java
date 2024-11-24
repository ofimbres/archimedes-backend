package com.binomiaux.archimedes.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.binomiaux.archimedes.model.Activity;
import com.binomiaux.archimedes.repository.entities.ActivityEntity;

@Mapper
public interface ActivityMapper {
    ActivityMapper INSTANCE = Mappers.getMapper( ActivityMapper.class );
 
    @Mapping(source = "activityId", target = "code")
    ActivityEntity activityToEntity(Activity entity);

    @Mapping(source = "code", target = "activityId")
    Activity entityToActivity(ActivityEntity model);
}
