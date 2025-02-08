package com.binomiaux.archimedes.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Health Check controller.
 */
@RestController
public class HealthCheckController {

    public HealthCheckController() {
    }
    
    @GetMapping("/")
    public ResponseEntity<String> get() {
        return ok("Health Check Success");
    }
}
