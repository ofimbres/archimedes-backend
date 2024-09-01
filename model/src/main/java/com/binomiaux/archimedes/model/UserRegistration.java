package com.binomiaux.archimedes.model;

public class UserRegistration {
    private String userId;
    private String username;
    private String userType;
    private boolean userConfirmed;

    public UserRegistration(String userId, String username, String userType, boolean userConfirmed) {
        this.userId = userId;
        this.username = username;
        this.userType = userType;
        this.userConfirmed = userConfirmed;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public boolean getUserConfirmed() {
        return userConfirmed;
    }

    public void setUserConfirmed(boolean userConfirmed) {
        this.userConfirmed = userConfirmed;
    }
}
