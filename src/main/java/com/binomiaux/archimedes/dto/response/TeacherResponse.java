package com.binomiaux.archimedes.dto.response;

/**
 * Response DTO for Teacher entity.
 * Hides DynamoDB internal fields from API responses.
 */
public class TeacherResponse {
    private String teacherId;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String schoolId;
    private Integer maxPeriods;

    public TeacherResponse() {}

    public TeacherResponse(String teacherId, String username, String firstName, String lastName, 
                          String fullName, String email, String schoolId, Integer maxPeriods) {
        this.teacherId = teacherId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.email = email;
        this.schoolId = schoolId;
        this.maxPeriods = maxPeriods;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public Integer getMaxPeriods() {
        return maxPeriods;
    }

    public void setMaxPeriods(Integer maxPeriods) {
        this.maxPeriods = maxPeriods;
    }

    @Override
    public String toString() {
        return "TeacherResponse{" +
                "teacherId='" + teacherId + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", schoolId='" + schoolId + '\'' +
                ", maxPeriods=" + maxPeriods +
                '}';
    }
}
