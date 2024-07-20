package com.binomiaux.archimedes.model;

public class School {
    private String schoolCode;
    private String name;

    public School() { 
    }

    public School(String schoolCode, String name) {
        this.schoolCode = schoolCode;
        this.name = name;
    }

    public String getId() {
        return schoolCode;
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
