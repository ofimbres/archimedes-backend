package com.binomiaux.archimedes.dto.response;

/**
 * Response DTO for school information.
 * Contains only the necessary information for API responses.
 */
public class SchoolResponse {

    private String id;
    private String schoolCode;
    private String name;
    private String address;
    private String principalName;
    private String contactEmail;
    private String phoneNumber;
    private int studentCount;
    private int teacherCount;
    private int periodCount;

    // Constructors
    public SchoolResponse() {}

    public SchoolResponse(String id, String schoolCode, String name) {
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

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    public int getTeacherCount() {
        return teacherCount;
    }

    public void setTeacherCount(int teacherCount) {
        this.teacherCount = teacherCount;
    }

    public int getPeriodCount() {
        return periodCount;
    }

    public void setPeriodCount(int periodCount) {
        this.periodCount = periodCount;
    }

    /**
     * Converts a School entity to a SchoolResponse DTO.
     * 
     * @param school the School entity
     * @return the SchoolResponse DTO
     */
    public static SchoolResponse fromSchool(com.binomiaux.archimedes.model.School school) {
        if (school == null) return null;
        
        SchoolResponse response = new SchoolResponse();
        response.setId(school.getId());
        response.setSchoolCode(school.getSchoolCode());
        response.setName(school.getName());
        response.setAddress(school.getAddress());
        response.setPrincipalName(school.getPrincipalName());
        response.setContactEmail(school.getContactEmail());
        response.setPhoneNumber(school.getPhoneNumber());
        response.setStudentCount(school.getStudentCount());
        response.setTeacherCount(school.getTeacherCount());
        response.setPeriodCount(school.getPeriodCount());
        
        return response;
    }
}
