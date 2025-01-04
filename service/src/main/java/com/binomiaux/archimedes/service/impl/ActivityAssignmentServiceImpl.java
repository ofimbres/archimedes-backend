package com.binomiaux.archimedes.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.model.ActivityAssignment;
import com.binomiaux.archimedes.model.ActivityAssignmentStatus;
import com.binomiaux.archimedes.service.ActivityAssignmentService;

@Service
public class ActivityAssignmentServiceImpl implements ActivityAssignmentService {

    @Override
    public ActivityAssignment createActivityAssignment(ActivityAssignment activityAssignment) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createActivityAssignment'");
    }

    @Override
    public void deleteActivityAssignment(String periodId, String activityId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteActivityAssignment'");
    }

    @Override
    public List<ActivityAssignment> getActivityAssignmentsByPeriod(String periodId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getActivityAssignmentsByPeriod'");
    }

    @Override
    public List<ActivityAssignmentStatus> getActivityAssignmentStatusesByStudent(String periodId, String studentId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getActivityAssignmentStatusesByStudent'");
    }
}