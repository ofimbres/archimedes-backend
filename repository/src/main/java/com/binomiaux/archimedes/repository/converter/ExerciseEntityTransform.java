package com.binomiaux.archimedes.repository.converter;

import com.binomiaux.archimedes.model.dynamodb.ExerciseEntity;
import com.binomiaux.archimedes.model.pojo.Exercise;

public class ExerciseEntityTransform implements EntityConverter<ExerciseEntity, Exercise> {
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