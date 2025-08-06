package com.binomiaux.archimedes.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.model.ActivityAssignment;
import com.binomiaux.archimedes.model.ActivityAssignmentStatus;

@Service
public class ActivityAssignmentService {

    public ActivityAssignment createActivityAssignment(ActivityAssignment activityAssignment) {
        throw new UnsupportedOperationException("Unimplemented method 'createActivityAssignment'");
    }

    public void deleteActivityAssignment(String periodId, String activityId) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteActivityAssignment'");
    }

    public List<ActivityAssignment> getActivityAssignmentsByPeriod(String periodId) {
        throw new UnsupportedOperationException("Unimplemented method 'getActivityAssignmentsByPeriod'");
    }

    public List<ActivityAssignmentStatus> getActivityAssignmentStatusesByStudent(String periodId, String studentId) {
        throw new UnsupportedOperationException("Unimplemented method 'getActivityAssignmentStatusesByStudent'");
    }
}