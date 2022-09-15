package com.binomiaux.archimedes.database.dao;

import com.binomiaux.archimedes.model.Exercise;

public interface ExerciseDao {
    Exercise getByCode(String code);
}