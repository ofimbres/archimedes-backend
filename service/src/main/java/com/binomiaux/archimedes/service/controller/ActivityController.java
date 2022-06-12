package com.binomiaux.archimedes.service.controller;

import com.binomiaux.archimedes.DynamoDbClient;
import com.binomiaux.archimedes.business.ActivityService;
import com.binomiaux.archimedes.db.model.Activity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("activity")
public class ActivityController {

    //@Autowired
    private ActivityService mActivityService;

    /*@PostMapping("/create")
    public ResponseEntity create(@RequestBody Activity activity) {
        //mActivityService.create(activity);
        return ok(activity);
    }*/

    @GetMapping("/")
    public ResponseEntity getAll() {
        //Iterable<Activity> a = mActivityService.getAll();
        return ok("potro");
    }
}
