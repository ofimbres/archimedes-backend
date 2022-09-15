package com.binomiaux.archimedes.service.controller;

import com.binomiaux.archimedes.business.ExerciseService;
import com.binomiaux.archimedes.business.ExerciseResultService;
import com.binomiaux.archimedes.model.ExerciseResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private ExerciseResultService mExerciseResultService;

    @Data
    static class SubmitExerciseResultsRequest {
        private String worksheetContentCopy;
        private String exerciseCode;
        private int score;
    }

    @CrossOrigin
    @PostMapping("/submit-exercise-results")
    public ResponseEntity submitExerciseResults(Principal principal, @RequestBody SubmitExerciseResultsRequest request) {
        long timestamp = Instant.now().toEpochMilli();
        String userId = principal.getName();
        mExerciseService.createExerciseResults(request.getExerciseCode(), userId, request.getWorksheetContentCopy(), request.getScore(), timestamp);
        return ok("potro");
    }

    @Data
    static class GetExerciseResultByClassAndExerciseRequest {
        private String exerciseCode;
        private String className;
    }

    @CrossOrigin
    @GetMapping("/get-latest-exercise-results")
    public ResponseEntity getLatestResults(/*@RequestBody GetExerciseResultByClassAndExerciseRequest request*/) {
        List<ExerciseResult> exerciseResults = mExerciseResultService.getByClassAndExercise("e46e7191-e31d-434a-aba3-b9a9c187a632", "WN16");
        return ok(exerciseResults);
    }
}
