package com.binomiaux.archimedes.business;

import com.binomiaux.archimedes.Exercise;

import java.util.List;

public interface ExerciseService {
    List<Exercise> getAll();
    Exercise getExerciseByCode(String code);
    String getLatestExerciseResults(String code, String studentId);
    void createExerciseResults(String code, String studentId, String worksheetCopy, int score, long timestamp);
}
