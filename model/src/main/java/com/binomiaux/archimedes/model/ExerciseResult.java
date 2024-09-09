package com.binomiaux.archimedes.model;

import java.time.Instant;

public class ExerciseResult {

    private Exercise exercise;
    private Student student;
    private Period period;
    private int score;
    private Instant timestamp;
    private String worksheetContent;
    private String s3Key;

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
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

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }
}
