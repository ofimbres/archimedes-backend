package com.binomiaux.archimedes.repository.api;

import com.binomiaux.archimedes.model.Exercise;

import java.util.List;

public interface ExerciseRepository {
    Exercise findByCode(String exerciseCode);
    List<Exercise> findByTopic(String topicId);
    List<Exercise> findByTopicAndSubtopic(String topicId, String subtopicId);
}