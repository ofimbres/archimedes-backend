package com.binomiaux.archimedes.service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("test")
public class TestController {

    @CrossOrigin
    @GetMapping("/hello")
    public ResponseEntity hello(Principal principal) {
        return ok("potro " + principal.getName());
    }
}
