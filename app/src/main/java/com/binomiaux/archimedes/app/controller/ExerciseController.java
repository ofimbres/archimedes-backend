package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.model.Exercise;
import com.binomiaux.archimedes.service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Exercise controller.
 */
@RestController
@RequestMapping("/api/v1/exercise")
public class ExerciseController {

    @Autowired
    private ExerciseService exerciseService;

    @GetMapping("/{exerciseId}")
    public ResponseEntity<Exercise> get(@PathVariable String exerciseId) {
        return ok(exerciseService.getExercise(exerciseId));
    }
}
