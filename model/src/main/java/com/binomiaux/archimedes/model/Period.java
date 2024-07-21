package com.binomiaux.archimedes.model;

public class Period {
    private String schoolId;
    private String teacherId;
    private String periodId;
    private String name;

    public Period() {
    }

    public Period(String schoolId, String teacherId, String periodId, String name) {
        this.schoolId = schoolId;
        this.teacherId = teacherId;
        this.periodId = periodId;
        this.name = name;
    }

    public String getPeriodId() {
        return periodId;
    }

    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
