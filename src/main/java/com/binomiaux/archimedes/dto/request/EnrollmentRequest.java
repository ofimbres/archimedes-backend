package com.binomiaux.archimedes.dto.request;

/**
 * Request DTO for student enrollment operations.
 * Uses simplified IDs: studentId (S001), periodId (T001-P001).
 */
public class EnrollmentRequest {
    
    private String studentId;  // e.g., "S001"
    private String periodId;   // e.g., "T001-P001"
    
    public EnrollmentRequest() {}
    
    public EnrollmentRequest(String studentId, String periodId) {
        this.studentId = studentId;
        this.periodId = periodId;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getPeriodId() {
        return periodId;
    }
    
    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }
}
