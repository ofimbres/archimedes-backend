package com.binomiaux.archimedes.business;

import com.binomiaux.archimedes.model.ExerciseResult;

import java.util.List;

public interface ExerciseResultService {
    void create(ExerciseResult exerciseResult);
    List<ExerciseResult> getByClassAndExercise(String className, String exerciseCode);
}
