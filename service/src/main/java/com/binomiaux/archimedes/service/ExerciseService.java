package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.Exercise;

import java.util.List;

public interface ExerciseService {
    Exercise getExercise(String exerciseId);
    List<Exercise> getExercisesByTopicIdAndSubtopicId(String topicId, String subtopicId);
}
