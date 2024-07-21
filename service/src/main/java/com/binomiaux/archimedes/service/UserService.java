package com.binomiaux.archimedes.service;

import java.util.Map;

import com.binomiaux.archimedes.model.LoggedInUser;
import com.binomiaux.archimedes.model.UserRegistration;

public interface UserService {
    UserRegistration registerUser(String username, String password, String email,  String givenName, String familyName, String schoolCode, String userType);
    LoggedInUser loginUser(String username, String password);
    Map<String, String> getUserAttributes(String username);
    void sendCode(String username);
    boolean verifyCode(String username, String confirmationCode);
    void forgotPassword(String username);
    void confirmForgotPassword(String username, String newPassword, String confirmationCode);
}