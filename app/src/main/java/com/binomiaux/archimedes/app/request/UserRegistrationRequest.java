package com.binomiaux.archimedes.app.request;

public class UserRegistrationRequest {
    private String username;
    private String password;
    private String email;
    private String givenName;
    private String familyName;
    private String userType;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getUserType() {
        return userType;
    }
}
