package com.binomiaux.archimedes.repository.transform;

import com.binomiaux.archimedes.repository.schema.ExerciseRecord;
import com.binomiaux.archimedes.model.Exercise;

public class ExerciseRecordTransform implements RecordTransform<ExerciseRecord, Exercise> {
    @Override
    public Exercise transform(ExerciseRecord entity) {
        Exercise model = new Exercise(entity.getCode(), entity.getName(), entity.getClassification(), entity.getPath());
        return model;
    }

    @Override
    public ExerciseRecord untransform(Exercise model) {
        return null;
    }
}
