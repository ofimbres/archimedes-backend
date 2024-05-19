package com.binomiaux.archimedes.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Health Check controller.
 */
@RestController
@RequestMapping("healthcheck")
public class HealthCheckController {
    public HealthCheckController() {
    }
    
    @GetMapping("/")
    public ResponseEntity<String> get() {
        return ok("Health Check Success");
    }
}
