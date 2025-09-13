package com.binomiaux.archimedes.dto.request;

/**
 * Request DTO for user registration.
 */
public class RegisterUserRequest {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String schoolCode;
    private String userType; // STUDENT, TEACHER

    public RegisterUserRequest() {
    }

    public RegisterUserRequest(String username, String email, String password, String firstName, 
                              String lastName, String schoolCode, String userType) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.schoolCode = schoolCode;
        this.userType = userType;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
