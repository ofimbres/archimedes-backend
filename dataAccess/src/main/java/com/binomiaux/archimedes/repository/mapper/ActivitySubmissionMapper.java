package com.binomiaux.archimedes.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.binomiaux.archimedes.model.ActivitySubmission;
import com.binomiaux.archimedes.repository.entities.ActivitySubmissionEntity;

@Mapper
public interface ActivitySubmissionMapper {
    ActivitySubmissionMapper INSTANCE = Mappers.getMapper( ActivitySubmissionMapper.class );
 
    ActivitySubmissionEntity activityResultToEntity(ActivitySubmission entity);
    ActivitySubmission entityToActivityResult(ActivitySubmissionEntity model);
}

