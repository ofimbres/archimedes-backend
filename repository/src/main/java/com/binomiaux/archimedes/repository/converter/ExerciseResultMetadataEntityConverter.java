package com.binomiaux.archimedes.repository.converter;

import com.binomiaux.archimedes.model.dynamodb.ExerciseResultMetadataEntity;
import com.binomiaux.archimedes.model.pojo.ExerciseResultMetadata;

import java.time.Instant;

public class ExerciseResultMetadataEntityConverter implements EntityConverter<ExerciseResultMetadataEntity, ExerciseResultMetadata> {
    @Override
    public ExerciseResultMetadata transform(ExerciseResultMetadataEntity entity) {
        ExerciseResultMetadata model = new ExerciseResultMetadata(null, null, null, entity.getScore(), Instant.parse(entity.getTimestamp()));
        return model;
    }

    @Override
    public ExerciseResultMetadataEntity untransform(ExerciseResultMetadata model) {
        return null;
    }
}
