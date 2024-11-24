package com.binomiaux.archimedes.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.binomiaux.archimedes.model.ActivityScore;
import com.binomiaux.archimedes.repository.entities.ActivityScoreEntity;

@Mapper
public interface ActivityScoreMapper {
    ActivityScoreMapper INSTANCE = Mappers.getMapper( ActivityScoreMapper.class );
 
    //@Mapping(source = "numberOfSeats", target = "seatCount")
    ActivityScoreEntity activityResultToEntity(ActivityScore entity);
    ActivityScore entityToActivityResult(ActivityScoreEntity model);
}

