package com.binomiaux.archimedes.app.mapper;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.binomiaux.archimedes.app.request.ActivityResultRequest;
import com.binomiaux.archimedes.model.ActivityResult;

@Mapper
public interface ActivityResultMapper {
    ActivityResultMapper INSTANCE = Mappers.getMapper(ActivityResultMapper.class);

    @Mapping(target = "worksheetContent", source = "request.worksheetContent")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    @Mapping(target = "activity.activityId", source = "activityId")
    @Mapping(target = "student.studentId", source = "request.studentId")
    @Mapping(target = "period.periodId", source = "request.periodId")
    ActivityResult requestToActivityResult(ActivityResultRequest request, String activityId);

    default Optional<String> map(String value) {
        return Optional.ofNullable(value);
    }
}