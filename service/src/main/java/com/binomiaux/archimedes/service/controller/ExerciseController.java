package com.binomiaux.archimedes.service.controller;

import com.binomiaux.archimedes.DynamoDbClient;
import com.binomiaux.archimedes.Exercise;
import com.binomiaux.archimedes.business.ActivityService;
import com.binomiaux.archimedes.business.ExerciseService;
import com.binomiaux.archimedes.db.model.Activity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("exercise")
public class ExerciseController {

    @Autowired
    private ExerciseService mExerciseService;

    /*@PostMapping("/create")
    public ResponseEntity create(@RequestBody Activity activity) {
        //mActivityService.create(activity);
        return ok(activity);
    }*/

    //@CrossOrigin(origins = "http://localhost:3000")
    @CrossOrigin(origins = { "http://archimedesfrontend-env.eba-283qbcvk.us-west-2.elasticbeanstalk.com", "http://localhost:3000" })
    @GetMapping("/")
    public ResponseEntity get(String code) {
        Exercise exercise = mExerciseService.getExerciseByCode(code);
        //Iterable<Activity> a = mActivityService.getAll();
        return ok(exercise);
    }

    @CrossOrigin(origins = { "http://archimedesfrontend-env.eba-283qbcvk.us-west-2.elasticbeanstalk.com", "http://localhost:3000" })
    @PostMapping("/upload-exercise-results")
    public ResponseEntity upload(@RequestParam String worksheetContentCopy, @RequestParam String studentId, @RequestParam String exerciseCode, @RequestParam int score) {
        long timestamp = Instant.now().toEpochMilli();
        mExerciseService.createExerciseResults(exerciseCode, studentId, worksheetContentCopy, score, timestamp);
        return ok("potro");
    }

    @CrossOrigin(origins = { "http://archimedesfrontend-env.eba-283qbcvk.us-west-2.elasticbeanstalk.com", "http://localhost:3000" })
    @GetMapping("/get-latest-exercise-results")
    public ResponseEntity getLatestResults(@RequestParam String studentId, @RequestParam String exerciseCode) {
        String filename = mExerciseService.getLatestExerciseResults(exerciseCode, studentId);
        return ok(filename);
    }
}
