package com.binomiaux.archimedes.model;

public class Student {
    private String schoolCode;
    private String studentCode;
    private String firstName;
    private String lastName;
    private String email;
    private String username;

    public Student() {
    }

    public Student(String schoolCode, String studentCode, String firstName, String lastName, String email, String username) {
        this.schoolCode = schoolCode;
        this.studentCode = studentCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
    }

    public String getId() {
        return schoolCode + "-S" + studentCode;
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
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