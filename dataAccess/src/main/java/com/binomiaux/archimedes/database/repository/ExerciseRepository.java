package com.binomiaux.archimedes.database.repository;

import com.binomiaux.archimedes.model.Exercise;

public interface ExerciseRepository {
    Exercise findByCode(String code);
}