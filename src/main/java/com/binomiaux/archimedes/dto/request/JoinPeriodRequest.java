package com.binomiaux.archimedes.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for student joining a period via registration code.
 */
public class JoinPeriodRequest {

    @NotBlank(message = "Registration code is required")
    private String registrationCode;

    // Default constructor
    public JoinPeriodRequest() {}

    public JoinPeriodRequest(String registrationCode) {
        this.registrationCode = registrationCode;
    }

    // Getters and Setters
    public String getRegistrationCode() { return registrationCode; }
    public void setRegistrationCode(String registrationCode) { this.registrationCode = registrationCode; }

    @Override
    public String toString() {
        return "JoinPeriodRequest{" +
                "registrationCode='" + registrationCode + '\'' +
                '}';
    }
}
