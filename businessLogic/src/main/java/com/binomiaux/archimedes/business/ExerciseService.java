package com.binomiaux.archimedes.business;

import com.binomiaux.archimedes.model.Exercise;

import java.util.List;

public interface ExerciseService {
    Exercise getExercise(String id);
    List<Exercise> getExercises();
}
