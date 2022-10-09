package com.binomiaux.archimedes.model;

import lombok.Data;

import java.time.Instant;

@Data
public class ExerciseResultMetadata {
    private Exercise exercise;
    private Student student;
    private Classroom classroom;
    private int score;
    private Instant timestamp;
}
