package com.binomiaux.archimedes.app.mapper;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.binomiaux.archimedes.app.request.ActivitySubmissionRequest;
import com.binomiaux.archimedes.model.ActivitySubmission;

@Mapper
public interface ActivityResultMapper {
    ActivityResultMapper INSTANCE = Mappers.getMapper(ActivityResultMapper.class);

    @Mapping(target = "worksheetContent", source = "request.worksheetContent")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    @Mapping(target = "activity.activityId", source = "request.activityId")
    @Mapping(target = "student.studentId", source = "request.studentId")
    @Mapping(target = "period.periodId", source = "request.periodId")
    ActivitySubmission requestToActivityResult(ActivitySubmissionRequest request);

    default Optional<String> map(String value) {
        return Optional.ofNullable(value);
    }
}