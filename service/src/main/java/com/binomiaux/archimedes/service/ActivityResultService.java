package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.ActivityResult;
import com.binomiaux.archimedes.model.ActivityScore;

import java.util.List;

public interface ActivityResultService {
    void create(ActivityResult exerciseResult);
    
    List<ActivityScore> getScoresByPeriodAndStudent(String periodId, String studentId);
    List<ActivityScore> getScoresByPeriod(String periodId);
    ActivityScore getScoresByPeriodAndStudentAndActivity(String periodId, String studentId, String activityId);
    List<ActivityScore> getScoresByPeriodAndActivity(String periodId, String activityId);
}