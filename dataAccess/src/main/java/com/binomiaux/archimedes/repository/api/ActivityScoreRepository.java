package com.binomiaux.archimedes.repository.api;

import java.util.List;

import com.binomiaux.archimedes.model.ActivityScore;

public interface ActivityScoreRepository {
    ActivityScore findByPeriodAndStudentAndActivity(String periodId, String studentId, String activityId);
    List<ActivityScore> findByPeriodAndActivity(String periodId, String activityId);

    // List<ActivityScore> findByStudentIdAndDate(String studentId, String activityId);
    void create(ActivityScore activityScore);
}