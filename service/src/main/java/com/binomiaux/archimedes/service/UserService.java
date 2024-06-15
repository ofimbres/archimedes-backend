package com.binomiaux.archimedes.service;

import java.util.Map;

import com.binomiaux.archimedes.model.LoggedInUser;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;

public interface UserService {
    void registerUser(String username, String password, String email,  String givenName, String familyName, String userType);
    LoggedInUser loginUser(String username, String password);
    Map<String, String> getUserAttributes(String username);
}