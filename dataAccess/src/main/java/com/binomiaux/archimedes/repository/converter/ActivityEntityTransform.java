package com.binomiaux.archimedes.repository.converter;

import com.binomiaux.archimedes.model.Activity;
import com.binomiaux.archimedes.repository.entities.ActivityEntity;

public class ActivityEntityTransform implements EntityTransform<ActivityEntity, Activity> {
    @Override
    public Activity transform(ActivityEntity entity) {
        Activity model = new Activity(entity.getCode(), entity.getName(), entity.getClassification(), entity.getPath());
        return model;
    }

    @Override
    public ActivityEntity untransform(Activity model) {
        return null;
    }
}
