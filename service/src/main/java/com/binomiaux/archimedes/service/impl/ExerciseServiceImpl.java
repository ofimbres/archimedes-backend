package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.ExerciseService;
import com.binomiaux.archimedes.model.Exercise;
import com.binomiaux.archimedes.repository.api.ExerciseRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciseServiceImpl implements ExerciseService {
    private static final Logger log = LoggerFactory.getLogger(ExerciseServiceImpl.class);

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Override
    public Exercise getExercise(String exerciseId) {
        return exerciseRepository.findByCode(exerciseId);
    }

    @Override
    public List<Exercise> getExercisesByTopicId(String topicId) {
        return exerciseRepository.findByTopic(topicId);
    }

    @Override
    public List<Exercise> getExercisesByTopicIdAndSubtopicId(String topicId, String subtopicId) {
        return exerciseRepository.findByTopicAndSubtopic(topicId, subtopicId);
    }
}
