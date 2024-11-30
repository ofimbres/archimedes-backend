package com.binomiaux.archimedes.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.binomiaux.archimedes.model.Topic;
import com.binomiaux.archimedes.repository.entities.TopicEntity;

@Mapper
public interface TopicMapper {
    TopicMapper INSTANCE = Mappers.getMapper( TopicMapper.class );
 
    TopicEntity activityResultToEntity(Topic model);
    Topic entityToActivityResult(TopicEntity entity);
}