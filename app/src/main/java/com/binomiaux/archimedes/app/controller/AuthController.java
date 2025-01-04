package com.binomiaux.archimedes.app.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.binomiaux.archimedes.app.request.ConfirmForgotPasswordRequest;
import com.binomiaux.archimedes.app.request.ForgotPasswordRequest;
import com.binomiaux.archimedes.app.request.SendCodeRequest;
import com.binomiaux.archimedes.app.request.UserLoginRequest;
import com.binomiaux.archimedes.app.request.UserRegistrationRequest;
import com.binomiaux.archimedes.app.request.VerifyCodeRequest;
import com.binomiaux.archimedes.model.LoggedInUser;
import com.binomiaux.archimedes.model.UserRegistration;
import com.binomiaux.archimedes.service.UserService;

/**
 * Auth controller.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegistration registerUser(@RequestBody UserRegistrationRequest userRequest) {
        UserRegistration userRegistration = userService.registerUser(userRequest.getUsername(), userRequest.getPassword(), userRequest.getEmail(), userRequest.getGivenName(), userRequest.getFamilyName(), userRequest.getSchoolCode(), userRequest.getUserType());
        return userRegistration;
    }

    @PostMapping("/login")
    public LoggedInUser loginUser(@RequestBody UserLoginRequest userRequest) {
        LoggedInUser loggedInUser = userService.loginUser(userRequest.getUsername(), userRequest.getPassword());
        return loggedInUser;
    }

    @GetMapping("/{username}/attributes")
    public Map<String, String> getUserAttributes(@PathVariable String username) {
        Map<String, String> userAttributes = userService.getUserAttributes(username);
        return userAttributes;
    }

    @PostMapping("/send-code")
    public void sendCode(@RequestBody SendCodeRequest request) {
        userService.sendCode(request.getUsername());
    }

    @PostMapping("/verify-code")
    public void verifyCode(@RequestBody VerifyCodeRequest request) {
        userService.verifyCode(request.getUsername(), request.getConfirmationCode());
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request.getUsername());
    }

    @PostMapping("/confirm-forgot-password")
    public void confirmForgotPassword(@RequestBody ConfirmForgotPasswordRequest request) {
        userService.confirmForgotPassword(request.getUsername(), request.getNewPassword(), request.getConfirmationCode());
    }
}