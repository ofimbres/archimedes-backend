package com.binomiaux.archimedes.controller;

import static org.springframework.http.ResponseEntity.ok;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.service.IdGeneratorService;

/**
 * Health Check controller.
 */
@RestController
@RequestMapping("/healthcheck")
public class HealthCheckController {

    @Autowired
    private IdGeneratorService idGeneratorService;

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

    @GetMapping("/dynamo-test")
    public ResponseEntity<Map<String, Object>> testDynamoDb() {
        try {
            idGeneratorService.testDynamoDbConnection();
            return ok(Map.of(
                "status", "SUCCESS",
                "message", "DynamoDB test completed - check server logs for details",
                "timestamp", Instant.now().toString()
            ));
        } catch (Exception e) {
            return ok(Map.of(
                "status", "ERROR", 
                "message", "DynamoDB test failed: " + e.getMessage(),
                "timestamp", Instant.now().toString()
            ));
        }
    }
}
