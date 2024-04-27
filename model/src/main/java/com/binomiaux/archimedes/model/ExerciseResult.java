package com.binomiaux.archimedes.model;

import java.time.Instant;

public record ExerciseResult(Exercise exercise, Student student, Classroom classroom, int score, Instant timestamp, String worksheetContent, String s3Key) { }