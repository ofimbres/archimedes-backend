package com.binomiaux.archimedes.database.transform;

import com.binomiaux.archimedes.database.schema.ExerciseResultMetadataRecord;
import com.binomiaux.archimedes.model.ExerciseResultMetadata;

import java.time.Instant;

public class ExerciseResultMetadataTransform implements RecordTransform<ExerciseResultMetadataRecord, ExerciseResultMetadata> {
    @Override
    public ExerciseResultMetadata transform(ExerciseResultMetadataRecord entity) {
        ExerciseResultMetadata model = new ExerciseResultMetadata();

        model.setScore(entity.getScore());
        model.setTimestamp(Instant.parse(entity.getTimestamp()));

        return model;
    }

    @Override
    public ExerciseResultMetadataRecord untransform(ExerciseResultMetadata model) {
        return null;
    }
}
