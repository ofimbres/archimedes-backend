package com.binomiaux.archimedes.model;

import java.time.LocalDate;

public class ActivityAssignmentStatus {
    private String activityId;
    private String activityName;
    private String activityDescription;
    private LocalDate dueDate;
    private String status;

    public ActivityAssignmentStatus() {
    }

    public ActivityAssignmentStatus(String activityId, String activityName, String activityDescription,
            LocalDate dueDate, String status) {
        this.activityId = activityId;
        this.activityName = activityName;
        this.activityDescription = activityDescription;
        this.dueDate = dueDate;
        this.status = status;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityDescription() {
        return activityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        this.activityDescription = activityDescription;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}