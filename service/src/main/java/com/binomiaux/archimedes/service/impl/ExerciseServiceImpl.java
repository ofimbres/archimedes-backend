package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.ExerciseService;
import com.binomiaux.archimedes.repository.ExerciseRepository;
import com.binomiaux.archimedes.model.Exercise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ExerciseServiceImpl implements ExerciseService {
    /*Map<String, Exercise> exerciseDb = new HashMap<>();

    public ExerciseServiceImpl() {
        Exercise exercise1 = new Exercise();
        exercise1.setId("WN16");
        exercise1.setName("Division & Multiplication Whole Numbers");
        exercise1.setClassification("miniquiz");

        Exercise exercise2 = new Exercise();
        exercise2.setId("EX02");
        exercise2.setName("Algebra Expressions");
        exercise2.setClassification("miniquiz");

        Exercise exercise3 = new Exercise();
        exercise3.setId("AN01");
        exercise3.setName("Angles Complementary");
        exercise3.setClassification("miniquiz");

        Exercise exercise4 = new Exercise();
        exercise4.setId("CI01");
        exercise4.setName("Area & Circumference Circle");
        exercise4.setClassification("miniquiz");

        Exercise exercise5 = new Exercise();
        exercise5.setId("GE04");
        exercise5.setName("Area & Perimeter Rectangle");
        exercise5.setClassification("miniquiz");

        Exercise exercise6 = new Exercise();
        exercise6.setId("VO10");
        exercise6.setName("Area & Volume Triangular Prism");
        exercise6.setClassification("miniquiz");

        exerciseDb.put(exercise1.getId(), exercise1);
        exerciseDb.put(exercise2.getId(), exercise2);
        exerciseDb.put(exercise3.getId(), exercise3);
        exerciseDb.put(exercise4.getId(), exercise4);
        exerciseDb.put(exercise5.getId(), exercise5);
        exerciseDb.put(exercise6.getId(), exercise6);
    }*/

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
