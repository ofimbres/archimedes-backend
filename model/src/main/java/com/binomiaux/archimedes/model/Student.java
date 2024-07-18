package com.binomiaux.archimedes.model;

import java.util.List;

public class Student {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String schoolCode;
    private String username;
    //private List<Attends> attends;

    public Student() {
    }

    public Student(String id, String firstName, String lastName, String email, String schoolCode, String username) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.schoolCode = schoolCode;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /*public List<Attends> getAttends() {
        return attends;
    }

    public void setAttends(List<Attends> attends) {
        this.attends = attends;
    }*/

    /*public static class Attends {
        public String teacherCode;
        public String periodCode;

        public String getTeacherCode() {
            return teacherCode;
        }

        public void setTeacherCode(String teacherCode) {
            this.teacherCode = teacherCode;
        }

        public String getPeriodCode() {
            return periodCode;
        }

        public void setPeriodCode(String periodCode) {
            this.periodCode = periodCode;
        }
    }*/
}