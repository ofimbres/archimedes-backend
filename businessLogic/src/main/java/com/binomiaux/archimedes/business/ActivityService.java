package com.binomiaux.archimedes.business;

import com.binomiaux.archimedes.db.model.Activity;

import java.util.List;

public interface ActivityService {
    void create(Activity activity);
    List<Activity> getAll();
}
