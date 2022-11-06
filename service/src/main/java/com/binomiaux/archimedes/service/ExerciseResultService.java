package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.ExerciseResult;

import java.util.List;

public interface ExerciseResultService {
    void create(ExerciseResult exerciseResult);
    Iterable<ExerciseResult> getByStudent(String classId, String studentId, String exerciseCode);
    ExerciseResult getByStudentAndExercise(String classId, String studentId, String exerciseCode);
    List<ExerciseResult> getByClassAndExercise(String className, String exerciseCode);
}
