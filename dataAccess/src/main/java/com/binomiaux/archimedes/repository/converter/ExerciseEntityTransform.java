package com.binomiaux.archimedes.repository.converter;

import com.binomiaux.archimedes.model.Exercise;
import com.binomiaux.archimedes.repository.entities.ExerciseEntity;

public class ExerciseEntityTransform implements EntityTransform<ExerciseEntity, Exercise> {
    @Override
    public Exercise transform(ExerciseEntity entity) {
        Exercise model = new Exercise(entity.getCode(), entity.getName(), entity.getClassification(), entity.getPath());
        return model;
    }

    @Override
    public ExerciseEntity untransform(Exercise model) {
        return null;
    }
}
