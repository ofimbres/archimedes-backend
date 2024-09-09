package com.binomiaux.archimedes.model;

import java.time.Instant;

public class ExerciseScore {
    private Exercise exercise;
    private Student student;
    private Period period;
    private int tries;
    private int bestScore;
    private String bestExerciseResult;
    //private Instant timestamp;

    public ExerciseScore() {
    }

    public ExerciseScore(Exercise exercise, Student student, Period period, int tries, int bestScore, String bestExerciseResult/*, Instant timestamp*/) {
        this.exercise = exercise;
        this.student = student;
        this.period = period;
        this.tries = tries;
        this.bestScore = bestScore;
        this.bestExerciseResult = bestExerciseResult;
        //this.timestamp = timestamp;
    }

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

    public int getTries() {
        return tries;
    }

    public void setTries(int tries) {
        this.tries = tries;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public String getBestExerciseResult() {
        return bestExerciseResult;
    }

    public void setBestExerciseResult(String bestExerciseResult) {
        this.bestExerciseResult = bestExerciseResult;
    }

    /*public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }*/
}
