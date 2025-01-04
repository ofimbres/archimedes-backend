package com.binomiaux.archimedes.model;

import java.time.Instant;

public class ActivityScore {
    private Activity activity;
    private Student student;
    private Period period;
    private int tries;
    private int score;
    private ActivitySubmission activitySubmission;
    private Instant timestamp;

    public ActivityScore() {
    }

    public ActivityScore(Activity activity, Student student, Period period, int tries, int score, ActivitySubmission activitySubmission, Instant timestamp) {
        this.activity = activity;
        this.student = student;
        this.period = period;
        this.tries = tries;
        this.score = score;
        this.activitySubmission = activitySubmission;
        this.timestamp = timestamp;
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

    public ActivitySubmission getActivitySubmission() {
        return activitySubmission;
    }

    public void setActivitySubmission(ActivitySubmission activitySubmission) {
        this.activitySubmission = activitySubmission;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
