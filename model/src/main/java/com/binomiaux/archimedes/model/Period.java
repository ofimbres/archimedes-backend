package com.binomiaux.archimedes.model;

public class Period {
    private String schoolCode;
    private String teacherCode;
    private String periodCode;
    private String name;

    public Period() {
    }

    public Period(String schoolCode, String teacherCode, String periodCode, String name) {
        this.schoolCode = schoolCode;
        this.teacherCode = teacherCode;
        this.periodCode = periodCode;
        this.name = name;
    }

    public String getId() {
        return schoolCode + "-T" + teacherCode + "-" + periodCode;
    }

    public String getPeriodCode() {
        return periodCode;
    }

    public void setPeriodCode(String periodCode) {
        this.periodCode = periodCode;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
