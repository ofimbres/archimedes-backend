package com.binomiaux.archimedes.model;

import lombok.Data;

import java.time.Instant;

@Data
public class ExerciseResult {

    private Exercise exercise;
    private Student student;
    private Period classroom;
    private int score;
    private Instant timestamp;
    private String worksheetContent;
    private String s3Key;
}
