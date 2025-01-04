package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.ActivityScore;
import com.binomiaux.archimedes.model.ActivitySubmission;

import java.util.List;

public interface ActivitySubmissionService {
    ActivitySubmission createActivitySubmission(ActivitySubmission activitySubmission);
    
    List<ActivityScore> getScoresByPeriodAndStudent(String periodId, String studentId);
    List<ActivityScore> getScoresByPeriod(String periodId);
    ActivityScore getScoresByPeriodAndStudentAndActivity(String periodId, String studentId, String activityId);
    List<ActivityScore> getScoresByPeriodAndActivity(String periodId, String activityId);
}