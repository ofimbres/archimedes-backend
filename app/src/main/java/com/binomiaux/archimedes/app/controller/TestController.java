package com.binomiaux.archimedes.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static org.springframework.http.ResponseEntity.ok;

/**
 *
 */
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/hello")
    public ResponseEntity hello(Principal principal) {
        return ok("potro " + principal.getName());
    }
}
