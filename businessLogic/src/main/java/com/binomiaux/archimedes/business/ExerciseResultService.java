package com.binomiaux.archimedes.business;

import com.binomiaux.archimedes.model.ExerciseResult;

import java.util.List;

public interface ExerciseResultService {
    void create(ExerciseResult exerciseResult);
    ExerciseResult getByClassStudentAndExercise(String classId, String studentId, String exerciseCode);
    List<ExerciseResult> getByClassAndExercise(String className, String exerciseCode);
}
