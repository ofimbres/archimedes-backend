package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.service.ExerciseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("/api/v1/exercise")
public class ExerciseController {

    @Autowired
    private ExerciseService mExerciseService;

    @GetMapping("/{exerciseId}")
    public ResponseEntity get(@PathVariable String exerciseId) {
        return ok("");
    }
}
