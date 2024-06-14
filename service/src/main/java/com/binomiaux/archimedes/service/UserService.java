package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.LoggedInUser;

public interface UserService {
    void registerUser(String username, String password, String email,  String givenName, String familyName, String userType);
    LoggedInUser loginUser(String username, String password);
}