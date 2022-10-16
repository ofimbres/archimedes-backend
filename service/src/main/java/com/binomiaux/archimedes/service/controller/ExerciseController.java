package com.binomiaux.archimedes.service.controller;

import com.binomiaux.archimedes.business.ExerciseService;
import com.binomiaux.archimedes.business.ExerciseResultService;
import com.binomiaux.archimedes.model.Classroom;
import com.binomiaux.archimedes.model.Exercise;
import com.binomiaux.archimedes.model.ExerciseResult;
import com.binomiaux.archimedes.model.Student;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("exercise")
public class ExerciseController {

    @Autowired
    private ExerciseService mExerciseService;

    @GetMapping("/")
    public ResponseEntity get() {
        List<Exercise> exerciseResults = mExerciseService.getExercises();
        return ok(exerciseResults);
    }
}
