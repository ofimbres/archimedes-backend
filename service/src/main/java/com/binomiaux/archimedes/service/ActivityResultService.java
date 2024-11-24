package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.ActivityResult;
import com.binomiaux.archimedes.model.ActivityScore;

import java.util.List;

public interface ActivityResultService {
    void create(ActivityResult exerciseResult);
    Iterable<ActivityResult> getByStudent(String classId, String studentId, String exerciseCode);
    ActivityScore getByStudentAndExercise(String classId, String studentId, String exerciseCode);
    List<ActivityScore> getByClassAndExercise(String className, String exerciseCode);
}