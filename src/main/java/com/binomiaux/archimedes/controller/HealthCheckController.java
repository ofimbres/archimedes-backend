package com.binomiaux.archimedes.controller;

import static org.springframework.http.ResponseEntity.ok;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health Check controller.
 */
@RestController
@RequestMapping("/healthcheck")
public class HealthCheckController {

    public HealthCheckController() {
    }
    
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> get() {
        return ok(Map.of(
            "status", "UP",
            "message", "Health Check Success",
            "timestamp", Instant.now().toString(),
            "service", "archimedes-backend"
        ));
    }
}
