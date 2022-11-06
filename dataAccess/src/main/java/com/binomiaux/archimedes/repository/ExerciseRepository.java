package com.binomiaux.archimedes.repository;

import com.binomiaux.archimedes.model.Exercise;

import java.util.List;

public interface ExerciseRepository {
    Exercise findByCode(String code);
    List<Exercise> findAll();
}