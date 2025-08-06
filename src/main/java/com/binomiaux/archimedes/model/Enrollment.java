package com.binomiaux.archimedes.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Enrollment entity with improved DynamoDB design and denormalized data.
 * Stores student and period display information for efficient queries.
 */
@DynamoDbBean
public class Enrollment {
    
    // DynamoDB keys
    private String pk;                       // ENROLLMENT#ENR001
    private String sk;                       // #METADATA
    private String entityType;              // ENROLLMENT
    private String parentEntityKey;         // STUDENT#STU001 (GSI1PK)
    private String childEntityKey;          // ENROLLMENT#PER001 (GSI1SK)
    private String searchTypeKey;           // PERIOD#PER001 (GSI2PK)
    private String searchValueKey;          // ENROLLMENT#STU001 (GSI2SK)
    
    // Core enrollment fields
    private String enrollmentId;            // ENR001
    private String studentId;               // STU001
    private String periodId;                // PER001
    private String enrollmentDate;          // 2024-07-25
    private String status;                  // ACTIVE, DROPPED, COMPLETED
    
    // Denormalized student data (for display)
    private String studentFullName;         // "Diego Hinojosa"
    private String studentFirstName;        // "Diego"
    private String studentLastName;         // "Hinojosa"
    
    // Denormalized period data (for display)
    private String periodDisplayName;       // "Algebra I (Period 6)"
    private String periodName;              // "Algebra I"
    private String periodNumber;            // "6"
    private String teacherLastName;         // "Trevino"

    // Constructors
    public Enrollment() {}

    public Enrollment(String studentId, String periodId) {
        this.studentId = studentId;
        this.periodId = periodId;
        this.status = "ACTIVE";
    }

    public Enrollment(Student student, Period period) {
        this.studentId = student.getStudentId();
        this.periodId = period.getPeriodId();
        this.studentFullName = student.getFullName();
        this.studentFirstName = student.getFirstName();
        this.studentLastName = student.getLastName();
        this.periodName = period.getName();
        this.periodNumber = String.valueOf(period.getPeriodNumber());
        this.periodDisplayName = period.getName() + " (Period " + period.getPeriodNumber() + ")";
        this.status = "ACTIVE";
    }

    // DynamoDB getters/setters
    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi1")
    public String getParentEntityKey() {
        return parentEntityKey;
    }

    public void setParentEntityKey(String parentEntityKey) {
        this.parentEntityKey = parentEntityKey;
    }

    @DynamoDbSecondarySortKey(indexNames = "gsi1")
    public String getChildEntityKey() {
        return childEntityKey;
    }

    public void setChildEntityKey(String childEntityKey) {
        this.childEntityKey = childEntityKey;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi2")
    public String getSearchTypeKey() {
        return searchTypeKey;
    }

    public void setSearchTypeKey(String searchTypeKey) {
        this.searchTypeKey = searchTypeKey;
    }

    @DynamoDbSecondarySortKey(indexNames = "gsi2")
    public String getSearchValueKey() {
        return searchValueKey;
    }

    public void setSearchValueKey(String searchValueKey) {
        this.searchValueKey = searchValueKey;
    }

    // Business field getters/setters
    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
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

    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(String enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStudentFullName() {
        return studentFullName;
    }

    public void setStudentFullName(String studentFullName) {
        this.studentFullName = studentFullName;
    }

    public String getStudentFirstName() {
        return studentFirstName;
    }

    public void setStudentFirstName(String studentFirstName) {
        this.studentFirstName = studentFirstName;
    }

    public String getStudentLastName() {
        return studentLastName;
    }

    public void setStudentLastName(String studentLastName) {
        this.studentLastName = studentLastName;
    }

    public String getPeriodDisplayName() {
        return periodDisplayName;
    }

    public void setPeriodDisplayName(String periodDisplayName) {
        this.periodDisplayName = periodDisplayName;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public String getPeriodNumber() {
        return periodNumber;
    }

    public void setPeriodNumber(String periodNumber) {
        this.periodNumber = periodNumber;
    }

    public String getTeacherLastName() {
        return teacherLastName;
    }

    public void setTeacherLastName(String teacherLastName) {
        this.teacherLastName = teacherLastName;
    }

    public static final TableSchema<Enrollment> TABLE_SCHEMA = TableSchema.fromBean(Enrollment.class);
}
