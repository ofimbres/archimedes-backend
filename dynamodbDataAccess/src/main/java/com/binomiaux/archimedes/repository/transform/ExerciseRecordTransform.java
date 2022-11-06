package com.binomiaux.archimedes.repository.transform;

import com.binomiaux.archimedes.repository.schema.ExerciseRecord;
import com.binomiaux.archimedes.model.Exercise;

public class ExerciseRecordTransform implements RecordTransform<ExerciseRecord, Exercise> {
    @Override
    public Exercise transform(ExerciseRecord entity) {
        Exercise model = new Exercise();
        model.setId(entity.getCode());
        model.setName(entity.getName());
        model.setPath(entity.getPath());
        model.setClassification(entity.getClassification());

        return model;
    }

    @Override
    public ExerciseRecord untransform(Exercise model) {
        return null;
    }
}
