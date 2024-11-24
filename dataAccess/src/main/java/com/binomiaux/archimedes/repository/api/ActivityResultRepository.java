package com.binomiaux.archimedes.repository.api;

import com.binomiaux.archimedes.model.ActivityResult;

public interface ActivityResultRepository {
    ActivityResult find(String id);
    void create(ActivityResult activityResult);
}