package com.binomiaux.archimedes.repository.api;

import com.binomiaux.archimedes.model.ExerciseResult;

import java.util.List;

public interface ExerciseResultRepository {
    void create(ExerciseResult exerciseResult);
    List<ExerciseResult> findByStudentId(String classId, String studentId, String exerciseCode);
    ExerciseResult findByStudentIdAndExerciseCode(String classId, String studentId, String exerciseCode);
    List<ExerciseResult> findAllByClassIdAndExerciseCode(String classId, String exerciseCode);
}
