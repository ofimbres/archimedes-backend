package com.binomiaux.archimedes.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.app.request.UserLoginRequest;
import com.binomiaux.archimedes.app.request.UserRegistrationRequest;
import com.binomiaux.archimedes.model.LoggedInUser;
import com.binomiaux.archimedes.service.UserService;

/**
 * User controller.
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationRequest userRequest) {
        try {
            userService.registerUser(userRequest.getUsername(), userRequest.getPassword(), userRequest.getEmail(), userRequest.getGivenName(), userRequest.getFamilyName(), userRequest.getUserType());
            return ResponseEntity.ok("User registered successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error registering user: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest userRequest) {
        LoggedInUser loggedInUser = userService.loginUser(userRequest.getUsername(), userRequest.getPassword());
        if (loggedInUser != null) {
            return ResponseEntity.ok(loggedInUser);
        } else {
            return ResponseEntity.badRequest().body("Invalid username or password.");
        }
    }
}
