package com.binomiaux.archimedes.repository;

import com.binomiaux.archimedes.model.pojo.Exercise;

import java.util.List;

public interface ExerciseRepository {
    Exercise findByCode(String exerciseCode);
    List<Exercise> findByTopic(String topicId);
    List<Exercise> findByTopicAndSubtopic(String topicId, String subtopicId);
}