package com.binomiaux.archimedes.app.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.dto.request.ConfirmForgotPasswordRequest;
import com.binomiaux.archimedes.dto.request.ForgotPasswordRequest;
import com.binomiaux.archimedes.dto.request.SendCodeRequest;
import com.binomiaux.archimedes.dto.request.UserLoginRequest;
import com.binomiaux.archimedes.dto.request.UserRegistrationRequest;
import com.binomiaux.archimedes.dto.request.VerifyCodeRequest;
import com.binomiaux.archimedes.model.LoggedInUser;
import com.binomiaux.archimedes.model.UserRegistration;
import com.binomiaux.archimedes.service.UserService;

import jakarta.validation.Valid;

/**
 * Authentication controller handling user registration, login, and account management.
 * 
 * Provides RESTful endpoints for:
 * - User registration and verification
 * - User authentication
 * - Password management
 * - User attribute retrieval
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegistration> registerUser(@Valid @RequestBody UserRegistrationRequest userRequest) {
        UserRegistration userRegistration = userService.registerUser(
            userRequest.getUsername(), 
            userRequest.getPassword(), 
            userRequest.getEmail(), 
            userRequest.getGivenName(), 
            userRequest.getFamilyName(), 
            userRequest.getSchoolCode(), 
            userRequest.getUserType()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(userRegistration);
    }

    @PostMapping("/login")
    public ResponseEntity<LoggedInUser> loginUser(@Valid @RequestBody UserLoginRequest userRequest) {
        LoggedInUser loggedInUser = userService.loginUser(userRequest.username(), userRequest.password());
        return ResponseEntity.ok(loggedInUser);
    }

    @GetMapping("/{username}/attributes")
    public ResponseEntity<Map<String, String>> getUserAttributes(@PathVariable String username) {
        Map<String, String> userAttributes = userService.getUserAttributes(username);
        return ResponseEntity.ok(userAttributes);
    }

    @PostMapping("/send-code")
    public ResponseEntity<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        userService.sendCode(request.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Void> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        userService.verifyCode(request.getUsername(), request.getConfirmationCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request.username());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-forgot-password")
    public ResponseEntity<Void> confirmForgotPassword(@Valid @RequestBody ConfirmForgotPasswordRequest request) {
        userService.confirmForgotPassword(request.getUsername(), request.getNewPassword(), request.getConfirmationCode());
        return ResponseEntity.ok().build();
    }
}