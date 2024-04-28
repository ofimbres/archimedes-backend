package com.binomiaux.archimedes.model.pojo;

import java.time.Instant;

public record ExerciseResultMetadata(Exercise exercise, Student student, Classroom classroom, int score, Instant timestamp) {}