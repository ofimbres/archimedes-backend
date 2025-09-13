package com.binomiaux.archimedes.service;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.exception.business.InternalServerException;
import com.binomiaux.archimedes.exception.business.UserNotConfirmedException;
import com.binomiaux.archimedes.exception.business.UserNotFoundException;
import com.binomiaux.archimedes.model.LoggedInUser;
import com.binomiaux.archimedes.service.awsservices.CognitoService;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;

/**
 * UserService: Handles pure authentication and user session management.
 * 
 * Responsibilities:
 * - Authentication (login/logout)
 * - Password management
 * - User verification
 * - Session management
 * - Cognito integration
 * 
 * Does NOT handle:
 * - Domain entity creation (Teacher/Student)
 * - Business logic beyond authentication
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final CognitoService cognitoService;

    public UserService(CognitoService cognitoService) {
        this.cognitoService = cognitoService;
    }

    /**
     * Check if email is already registered in Cognito.
     */
    public boolean isEmailRegistered(String email) {
        return cognitoService.isEmailRegistered(email);
    }

    /**
     * Create authentication user in Cognito (called by RegistrationService).
     */
    public void createAuthenticationUser(String username, String password, String email, 
                                        String givenName, String familyName, String userType, String domainEntityId) {
        // Register user in Cognito
        cognitoService.signUpUser(username, password, email, givenName, familyName);
        cognitoService.addUserToGroup(userType, username);
        cognitoService.addUserAttribute(username, "custom:userId", domainEntityId);
    }

    public LoggedInUser loginUser(String username, String password) {
        try {
            log.info("Logging in user: {}", username);
            InitiateAuthResponse authResponse = cognitoService.loginUser(username, password);
            String accessToken = authResponse.authenticationResult().accessToken();
            //String idToken = authResponse.authenticationResult().idToken();
            //String refreshToken = authResponse.authenticationResult().refreshToken();

            LoggedInUser loggedInUser = new LoggedInUser(username, accessToken);
            return loggedInUser;
        } catch (software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotConfirmedException e) {
            throw new UserNotConfirmedException("User account is not confirmed.");
        } catch (software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException e) {
            throw new UserNotFoundException("User not found.");
        }
    }

    public Map<String, String> getUserAttributes(String username) {
        return cognitoService.getUserAttributes(username).userAttributes().stream()
            .collect(Collectors.toMap(AttributeType::name, AttributeType::value));
    }

    public void sendCode(String username) {
        try {
            cognitoService.sendCode(username);
        } catch (Exception e) {
            log.error("Error sending verification code for user: {}", username, e);
            throw new InternalServerException("Failed to send verification code");
        }
    }

    public boolean verifyCode(String username, String confirmationCode) {
        try {
            cognitoService.verifyCode(username, confirmationCode);
            return true;
        } catch (Exception e) {
            log.error("Error verifying code for user: {}", username, e);
            return false;
        }
    }

    public void forgotPassword(String username) {
        try {
            cognitoService.forgotPassword(username);
        } catch (Exception e) {
            log.error("Error initiating password reset for user: {}", username, e);
            throw new InternalServerException("Failed to initiate password reset");
        }
    }

    public void confirmForgotPassword(String username, String newPassword, String confirmationCode) {
        try {
            cognitoService.confirmForgotPassword(username, newPassword, confirmationCode);
        } catch (Exception e) {
            log.error("Error confirming password reset for user: {}", username, e);
            throw new InternalServerException("Failed to confirm password reset");
        }
    }
}
