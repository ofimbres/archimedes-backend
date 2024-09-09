package com.binomiaux.archimedes.app.request;

public class CreateReportRequest {
    private String worksheetContentCopy;
    private String exerciseId;
    private String periodId;
    private String studentId;
    private int score;

    public String getWorksheetContentCopy() {
        return worksheetContentCopy;
    }

    public void setWorksheetContentCopy(String worksheetContentCopy) {
        this.worksheetContentCopy = worksheetContentCopy;
    }

    public String getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getPeriodId() {
        return periodId;
    }

    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
