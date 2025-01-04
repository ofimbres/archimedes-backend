package com.binomiaux.archimedes.repository.api;

import com.binomiaux.archimedes.model.ActivitySubmission;

public interface ActivitySubmissionRepository {
    ActivitySubmission find(String id);
    void create(ActivitySubmission activityResult);
}