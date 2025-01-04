package com.binomiaux.archimedes.model;

import java.time.LocalDate;

public class ActivityAssignment {
    private String activityId;
    private String name;
    private String classification;
    private LocalDate dueDate;

    public ActivityAssignment() {
    }

    public ActivityAssignment(String activityId, String name, String classification, LocalDate dueDate) {
        this.activityId = activityId;
        this.name = name;
        this.classification = classification;
        this.dueDate = dueDate;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
