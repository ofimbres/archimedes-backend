package com.binomiaux.archimedes.repository.api;

import java.util.List;

import com.binomiaux.archimedes.model.ActivityScore;

public interface ActivityScoreRepository {
    ActivityScore find(String periodId, String studentId, String exerciseId);
    // List<ActivityScore> findByStudentIdAndActivityId(String studentId, String activityId);
    // List<ActivityScore> findByStudentIdAndDate(String studentId, String activityId);
    void create(ActivityScore activityScore);
}