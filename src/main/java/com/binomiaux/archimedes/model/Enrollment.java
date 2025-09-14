package com.binomiaux.archimedes.model;

import java.time.LocalDate;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Compatibility bridge for DynamoDB repositories.
 * Extends JPA entity but adds DynamoDB-specific functionality.
 */
@DynamoDbBean
public class Enrollment extends com.binomiaux.archimedes.entity.Enrollment {
    
    // DynamoDB-specific fields
    private String pk;
    private String sk;
    private String entityType;
    private String studentId;
    private String periodId;
    private String schoolId;
    private String studentFirstName;
    private String studentLastName;
    private String studentFullName;
    private String periodName;
    private String periodNumber;
    private String periodDisplayName;
    
    public Enrollment() {
        super();
    }
    
    public Enrollment(String enrollmentId, com.binomiaux.archimedes.entity.Student student, com.binomiaux.archimedes.entity.Period period) {
        super(enrollmentId, student, period);
        this.entityType = "ENROLLMENT";
    }
    
    // DynamoDB constructor for repository compatibility
    public Enrollment(String studentId, String periodId) {
        super();
        this.studentId = studentId;
        this.periodId = periodId;
        this.entityType = "ENROLLMENT";
    }
    
    // DynamoDB key methods
    public String getPk() { return pk; }
    public void setPk(String pk) { this.pk = pk; }
    
    public String getSk() { return sk; }
    public void setSk(String sk) { this.sk = sk; }
    
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    // DynamoDB denormalized fields
    public String getStudentId() { 
        return getStudent() != null ? getStudent().getStudentId() : studentId; 
    }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getPeriodId() { 
        return getPeriod() != null ? getPeriod().getPeriodId() : periodId; 
    }
    public void setPeriodId(String periodId) { this.periodId = periodId; }
    
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    
    public String getStudentFirstName() { return studentFirstName; }
    public void setStudentFirstName(String studentFirstName) { this.studentFirstName = studentFirstName; }
    
    public String getStudentLastName() { return studentLastName; }
    public void setStudentLastName(String studentLastName) { this.studentLastName = studentLastName; }
    
    public String getStudentFullName() { return studentFullName; }
    public void setStudentFullName(String studentFullName) { this.studentFullName = studentFullName; }
    
    public String getPeriodName() { return periodName; }
    public void setPeriodName(String periodName) { this.periodName = periodName; }
    
    public String getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(String periodNumber) { this.periodNumber = periodNumber; }
    
    public String getPeriodDisplayName() { return periodDisplayName; }
    public void setPeriodDisplayName(String periodDisplayName) { this.periodDisplayName = periodDisplayName; }
    
    // Override to handle String input from DynamoDB
    public void setEnrollmentDate(String enrollmentDate) {
        // Handle both String and LocalDate for DynamoDB compatibility
        if (enrollmentDate != null) {
            super.setEnrollmentDate(LocalDate.parse(enrollmentDate));
        }
    }
    
    // DynamoDB helper methods
    public void generateKeys() {
        if (studentId != null && periodId != null) {
            this.pk = "STUDENT#" + studentId;
            this.sk = "PERIOD#" + periodId;
        }
    }
    
    public static final TableSchema<Enrollment> TABLE_SCHEMA = TableSchema.fromBean(Enrollment.class);
}