package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.ExerciseService;
import com.binomiaux.archimedes.repository.ExerciseRepository;
import com.binomiaux.archimedes.model.Exercise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ExerciseServiceImpl implements ExerciseService {

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
