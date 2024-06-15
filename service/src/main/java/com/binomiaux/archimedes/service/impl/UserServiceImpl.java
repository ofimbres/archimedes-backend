package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.model.LoggedInUser;
import com.binomiaux.archimedes.service.UserService;
import com.binomiaux.archimedes.service.awsservices.CognitoService;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private CognitoService cognitoService;

    @Override
    public void registerUser(String username, String password, String email, String givenName, String familyName, String userType) {
        try {
            cognitoService.createUser(username, password, email, givenName, familyName);
            cognitoService.addUserToGroup(userType, username);
        } catch (Exception e) {
            // Handle register errors
            throw new RuntimeException("Error registering user : " + e.getMessage(), e);
        }
    }

    @Override
    public LoggedInUser loginUser(String username, String password) {
        try {
            AdminInitiateAuthResponse authResponse = cognitoService.loginUser(username, password);
            String accessToken = authResponse.authenticationResult().accessToken();
            //String idToken = authResponse.authenticationResult().idToken();
            //String refreshToken = authResponse.authenticationResult().refreshToken();

            LoggedInUser loggedInUser = new LoggedInUser(username, accessToken);
            return loggedInUser;
        } catch (Exception e) {
            // Handle login errors
            return null;
        }
    }

    @Override
    public Map<String, String> getUserAttributes(String username) {
        return cognitoService.getUserAttributes(username).userAttributes().stream()
            .collect(Collectors.toMap(AttributeType::name, AttributeType::value));
    }
}
