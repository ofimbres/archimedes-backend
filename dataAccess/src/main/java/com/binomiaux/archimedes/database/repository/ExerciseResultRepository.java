package com.binomiaux.archimedes.database.repository;

import com.binomiaux.archimedes.model.ExerciseResult;

import java.util.List;

public interface ExerciseResultRepository {
    void create(ExerciseResult exerciseResult);
    ExerciseResult findByClassIdStudentIdAndExerciseCode(String classId, String studentId, String exerciseCode);
    List<ExerciseResult> findByClassIdAndExerciseCode(String classId, String exerciseCode);
}
