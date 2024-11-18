package com.binomiaux.archimedes.model;

import java.time.Instant;

public class ExerciseScore {
    private Activity exercise;
    private Student student;
    private Period period;
    private int tries;
    private int score;
    private String exerciseResult;
    //private Instant timestamp;

    public ExerciseScore() {
    }

    public ExerciseScore(Activity exercise, Student student, Period period, int tries, int score, String exerciseResult/*, Instant timestamp*/) {
        this.exercise = exercise;
        this.student = student;
        this.period = period;
        this.tries = tries;
        this.score = score;
        this.exerciseResult = exerciseResult;
        //this.timestamp = timestamp;
    }

    public Activity getExercise() {
        return exercise;
    }

    public void setExercise(Activity exercise) {
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

    public int getTries() {
        return tries;
    }

    public void setTries(int tries) {
        this.tries = tries;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getExerciseResult() {
        return exerciseResult;
    }

    public void setExerciseResult(String exerciseResult) {
        this.exerciseResult = exerciseResult;
    }

    /*public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }*/
}
