package com.binomiaux.archimedes.model;

import lombok.Data;

import java.time.Instant;

@Data
public class ExerciseResult {
    //private String exerciseId;
    //private String exerciseName;
    private Exercise exercise;

    //private String studentId;
    //private String studentName;
    private Student student;

    private int score;
    private Instant timestamp;

    private String worksheetContent;
}
