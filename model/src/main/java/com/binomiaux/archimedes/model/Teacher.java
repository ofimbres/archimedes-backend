package com.binomiaux.archimedes.model;

public class Teacher {
    private String schoolCode;
    private String teacherCode;
    private String firstName;
    private String lastName;
    private String email;
    private String username;

    public Teacher() {
    }

    public Teacher(String schoolCode, String teacherCode, String firstName, String lastName, String email, String username) {
        this.schoolCode = schoolCode;
        this.teacherCode = teacherCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
    }

    public String getId() {
        return schoolCode + "-T" + teacherCode;
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
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
