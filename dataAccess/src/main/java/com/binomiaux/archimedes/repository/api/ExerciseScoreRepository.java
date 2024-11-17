package com.binomiaux.archimedes.repository.api;

import com.binomiaux.archimedes.model.ExerciseScore;

public interface ExerciseScoreRepository {
    ExerciseScore find(String periodId, String studentId, String exerciseId);
    void create(ExerciseScore exerciseResult);
}
