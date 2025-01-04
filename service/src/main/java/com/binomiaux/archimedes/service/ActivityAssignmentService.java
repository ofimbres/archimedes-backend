package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.ActivityAssignment;
import com.binomiaux.archimedes.model.ActivityAssignmentStatus;

import java.util.List;

public interface ActivityAssignmentService {
    ActivityAssignment createActivityAssignment(ActivityAssignment activityAssignment);
    void deleteActivityAssignment(String periodId, String activityId);
    List<ActivityAssignment> getActivityAssignmentsByPeriod(String periodId);

    List<ActivityAssignmentStatus> getActivityAssignmentStatusesByStudent(String periodId, String studentId);
}
