package com.binomiaux.archimedes.business;

import com.binomiaux.archimedes.Exercise;

public interface ExerciseService {
    Exercise getExerciseByCode(String code);
    String getLatestExerciseResults(String code, String studentId);
    void createExerciseResults(String code, String studentId, String worksheetCopy, int score, long timestamp);
}
