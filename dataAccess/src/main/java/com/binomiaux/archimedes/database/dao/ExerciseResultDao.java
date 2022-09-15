package com.binomiaux.archimedes.database.dao;

import com.binomiaux.archimedes.model.ExerciseResult;

import java.util.List;

public interface ExerciseResultDao {
    void create(ExerciseResult exerciseResult);
    ExerciseResult getByClassIdStudentIdAndExerciseCode(String classId, String studentId, String exerciseCode);
    List<ExerciseResult> getByClassIdAndExerciseCode(String classId, String exerciseCode);
}
