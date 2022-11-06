package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.Exercise;

public interface ExerciseService {
    Exercise getExercise(String id);
    Iterable<Exercise> getExercises();
}
