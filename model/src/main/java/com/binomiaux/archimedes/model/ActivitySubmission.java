package com.binomiaux.archimedes.model;

import java.time.Instant;

public class ActivitySubmission {

    private String activityResultId;
    private Activity activity;
    private Student student;
    private Period period;
    private int score;
    private Instant timestamp;
    private String worksheetContent;
    private String resourcePath;

    public String getActivityResultId() {
        return activityResultId;
    }

    public void setActivityResultId(String activityResultId) {
        this.activityResultId = activityResultId;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getWorksheetContent() {
        return worksheetContent;
    }

    public void setWorksheetContent(String worksheetContent) {
        this.worksheetContent = worksheetContent;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }
}
