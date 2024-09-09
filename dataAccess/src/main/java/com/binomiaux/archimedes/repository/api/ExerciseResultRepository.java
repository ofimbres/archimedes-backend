package com.binomiaux.archimedes.repository.api;

import com.binomiaux.archimedes.model.ExerciseResult;
import com.binomiaux.archimedes.model.ExerciseScore;

import java.util.List;

public interface ExerciseResultRepository {
    void create(ExerciseResult exerciseResult);
    List<ExerciseResult> findByStudentId(String classId, String studentId, String exerciseId);
    ExerciseScore findByStudentIdAndExerciseId(String classId, String studentId, String exerciseId);
    List<ExerciseScore> findAllByClassIdAndExerciseId(String classId, String exerciseId);
}
