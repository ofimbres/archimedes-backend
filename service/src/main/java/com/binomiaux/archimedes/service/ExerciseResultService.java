package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.ActivityResult;
import com.binomiaux.archimedes.model.ExerciseScore;

import java.util.List;

public interface ExerciseResultService {
    void create(ActivityResult exerciseResult);
    Iterable<ActivityResult> getByStudent(String classId, String studentId, String exerciseCode);
    ExerciseScore getByStudentAndExercise(String classId, String studentId, String exerciseCode);
    List<ExerciseScore> getByClassAndExercise(String className, String exerciseCode);
}