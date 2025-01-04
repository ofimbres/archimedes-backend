package com.binomiaux.archimedes.app.request;

public class ActivitySubmissionRequest {
    private String activityId;
    private String studentId;
    private String periodId;
    private String worksheetContent;
    private int score;

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getPeriodId() {
        return periodId;
    }

    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }

    public String getWorksheetContent() {
        return worksheetContent;
    }

    public void setWorksheetContent(String worksheetContent) {
        this.worksheetContent = worksheetContent;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
