package com.binomiaux.archimedes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Request DTO for creating a new teacher.
 */
public class CreateTeacherRequest {

    @NotBlank(message = "School ID is required")
    private String schoolId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Min(value = 1, message = "Max periods must be at least 1")
    @Max(value = 10, message = "Max periods cannot exceed 10")
    private Integer maxPeriods;

    // Default constructor
    public CreateTeacherRequest() {}

    public CreateTeacherRequest(String schoolId, String firstName, String lastName, String email, Integer maxPeriods) {
        this.schoolId = schoolId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.maxPeriods = maxPeriods;
    }

    // Getters and Setters
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getMaxPeriods() { return maxPeriods; }
    public void setMaxPeriods(Integer maxPeriods) { this.maxPeriods = maxPeriods; }

    @Override
    public String toString() {
        return "CreateTeacherRequest{" +
                "schoolId='" + schoolId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", maxPeriods=" + maxPeriods +
                '}';
    }
}
