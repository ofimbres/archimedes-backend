package com.binomiaux.archimedes.dto.response;

/**
 * Clean response DTO for Student data - only exposes necessary fields to API clients
 */
public class StudentResponse {
    
    private String studentId;
    private String schoolId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String username;

    // Constructors
    public StudentResponse() {}

    public StudentResponse(String studentId, String schoolId, String firstName, String lastName, 
                          String fullName, String email, String username) {
        this.studentId = studentId;
        this.schoolId = schoolId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.email = email;
        this.username = username;
    }

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
