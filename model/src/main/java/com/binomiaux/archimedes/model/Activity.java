package com.binomiaux.archimedes.model;

public class Activity {
    private String activityId;
    private String name;
    private String classification;
    private String path;
    private String url;

    public Activity() {
    }

    public Activity(String activityId, String name, String classification, String path) {
        this.activityId = activityId;
        this.name = name;
        this.classification = classification;
        this.path = path;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
