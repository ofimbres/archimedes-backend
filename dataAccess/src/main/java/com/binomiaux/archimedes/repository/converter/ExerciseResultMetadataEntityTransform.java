package com.binomiaux.archimedes.repository.converter;

import com.binomiaux.archimedes.model.ExerciseResultMetadata;
import com.binomiaux.archimedes.repository.entities.ExerciseResultMetadataEntity;

import java.time.Instant;

public class ExerciseResultMetadataEntityTransform implements EntityTransform<ExerciseResultMetadataEntity, ExerciseResultMetadata> {
    @Override
    public ExerciseResultMetadata transform(ExerciseResultMetadataEntity entity) {
        ExerciseResultMetadata model = new ExerciseResultMetadata();

        model.setScore(entity.getScore());
        model.setTimestamp(Instant.parse(entity.getTimestamp()));

        return model;
    }

    @Override
    public ExerciseResultMetadataEntity untransform(ExerciseResultMetadata model) {
        return null;
    }
}
