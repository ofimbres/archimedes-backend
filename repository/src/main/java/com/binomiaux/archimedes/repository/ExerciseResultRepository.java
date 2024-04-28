package com.binomiaux.archimedes.repository;

import com.binomiaux.archimedes.model.pojo.ExerciseResult;

import java.util.List;

public interface ExerciseResultRepository {
    void create(ExerciseResult exerciseResult);
    List<ExerciseResult> findByStudentId(String classId, String studentId, String exerciseCode);
    ExerciseResult findByStudentIdAndExerciseCode(String classId, String studentId, String exerciseCode);
    List<ExerciseResult> findAllByClassIdAndExerciseCode(String classId, String exerciseCode);
}
