package com.binomiaux.archimedes.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("healthcheck")
public class HealthCheckController {
    @GetMapping("/")
    public ResponseEntity get() {
        return ok("Health Check Success");
    }
}
