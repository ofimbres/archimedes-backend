package com.binomiaux.archimedes.dto.request;

/**
 * Request DTO for creating a new school.
 */
public class CreateSchoolRequest {

    private String id;
    private String schoolCode;
    private String name;
    private String address;
    private String principalName;
    private String contactEmail;
    private String phoneNumber;

    // Constructors
    public CreateSchoolRequest() {}

    public CreateSchoolRequest(String id, String schoolCode, String name) {
        this.id = id;
        this.schoolCode = schoolCode;
        this.name = name;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
