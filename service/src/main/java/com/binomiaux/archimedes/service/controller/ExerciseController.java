package com.binomiaux.archimedes.service.controller;

import com.binomiaux.archimedes.Exercise;
import com.binomiaux.archimedes.business.ExerciseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("exercise")
public class ExerciseController {

    @Autowired
    private ExerciseService mExerciseService;

    @GetMapping("/")
    public ResponseEntity getAll() {
        List<Exercise> exercises = mExerciseService.getAll();
        return ok(exercises);
    }

    @PostMapping("/submit-exercise-results")
    public ResponseEntity submitExerciseResults(Principal principal, @RequestParam String worksheetContentCopy, @RequestParam String exerciseCode, @RequestParam int score) {
        long timestamp = Instant.now().toEpochMilli();
        String userId = principal.getName();
        mExerciseService.createExerciseResults(exerciseCode, userId, worksheetContentCopy, score, timestamp);
        return ok("potro");
    }

    @GetMapping("/get-latest-exercise-results")
    public ResponseEntity getLatestResults(Principal principal, @RequestParam String exerciseCode) {
        String userId = principal.getName();
        String filename = mExerciseService.getLatestExerciseResults(exerciseCode, userId);
        return ok(filename);
    }
}
