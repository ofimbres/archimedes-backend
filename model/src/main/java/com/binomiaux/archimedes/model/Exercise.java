package com.binomiaux.archimedes.model;

public class Exercise {
    private String exerciseId;
    private String name;
    private String classification;
    private String path;

    public Exercise() {
    }

    public Exercise(String exerciseId, String name, String classification, String path) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.classification = classification;
        this.path = path;
    }

    public String getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
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
}
