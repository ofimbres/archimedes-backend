package com.binomiaux.archimedes.model;

public class Period {
    private String code;
    private String name;
    private String schoolCode;
    private String teacherCode;

    public Period() {
        
    }

    public Period(String code, String name, String schoolCode, String teacherCode) {
        this.code = code;
        this.name = name;
        this.schoolCode = schoolCode;
        this.teacherCode = teacherCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public static class AttendedBy {
        private String studentCode;
        private String firstName;
        private String lastName;

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
    }
}
