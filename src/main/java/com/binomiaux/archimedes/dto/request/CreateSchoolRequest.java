package com.binomiaux.archimedes.dto.request;

/**
 * Request DTO for creating a new school.
 */
public class CreateSchoolRequest {

    private String schoolCode;
    private String name;
    private String address;
    private String principal;
    private String email;
    private String phoneNumber;

    // Constructors
    public CreateSchoolRequest() {}

    public CreateSchoolRequest(String schoolCode, String name) {
        this.schoolCode = schoolCode;
        this.name = name;
    }

    // Getters and setters
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
