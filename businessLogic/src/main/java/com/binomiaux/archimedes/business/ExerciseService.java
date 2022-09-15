package com.binomiaux.archimedes.business;

import com.binomiaux.archimedes.model.Exercise;

public interface ExerciseService {
    Exercise getExerciseByCode(String code);
    void createExerciseResults(String code, String studentId, String worksheetCopy, int score, long timestamp);
}
