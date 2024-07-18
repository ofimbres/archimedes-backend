package com.binomiaux.archimedes.app.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.app.request.ConfirmForgotPasswordRequest;
import com.binomiaux.archimedes.app.request.ForgotPasswordRequest;
import com.binomiaux.archimedes.app.request.SendCodeRequest;
import com.binomiaux.archimedes.app.request.UserLoginRequest;
import com.binomiaux.archimedes.app.request.UserRegistrationRequest;
import com.binomiaux.archimedes.app.request.VerifyCodeRequest;
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
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest userRequest) {
        try {
            userService.registerUser(userRequest.getUsername(), userRequest.getPassword(), userRequest.getEmail(), userRequest.getGivenName(), userRequest.getFamilyName(), userRequest.getSchoolCode(), userRequest.getUserType());
            return ResponseEntity.ok().build();
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

    @GetMapping("/{username}/attributes")
    public ResponseEntity<?> getUserAttributes(@PathVariable String username) {
        try {
            Map<String, String> userAttributes = userService.getUserAttributes(username);
            return ResponseEntity.ok(userAttributes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving user attributes: " + e.getMessage());
        }
    }

    @PostMapping("/sendCode")
    public ResponseEntity<?> sendCode(@RequestBody SendCodeRequest request) {
        try {
            userService.sendCode(request.getUsername());
            return ResponseEntity.ok("Code sent successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending code: " + e.getMessage());
        }
    }

    @PostMapping("/verifyCode")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequest request) {
        try {
            userService.verifyCode(request.getUsername(), request.getConfirmationCode());
            return ResponseEntity.ok("User verified successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error verifying code: " + e.getMessage());
        }
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            userService.forgotPassword(request.getUsername());
            return ResponseEntity.ok("Password reset initiated successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error initiating password reset: " + e.getMessage());
        }
    }

    @PostMapping("/confirmForgotPassword")
    public ResponseEntity<?> confirmForgotPassword(@RequestBody ConfirmForgotPasswordRequest request) {
        try {
            userService.confirmForgotPassword(request.getUsername(), request.getNewPassword(), request.getConfirmationCode());
            return ResponseEntity.ok("Password reset confirmed successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error confirming password reset: " + e.getMessage());
        }
    }
}
