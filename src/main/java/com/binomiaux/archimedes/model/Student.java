package com.binomiaux.archimedes.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Compatibility bridge for DynamoDB repositories.
 * Extends JPA entity but adds DynamoDB-specific functionality.
 */
@DynamoDbBean
public class Student extends com.binomiaux.archimedes.entity.Student {
    
    // DynamoDB-specific fields
    private String pk;
    private String sk;
    private String entityType;
    
    public Student() {
        super();
    }
    
    public Student(String studentId, com.binomiaux.archimedes.entity.School school, String firstName, String lastName) {
        super(studentId, school, firstName, lastName);
        this.entityType = "STUDENT";
    }
    
    // DynamoDB key methods
    public String getPk() { return pk; }
    public void setPk(String pk) { this.pk = pk; }
    
    public String getSk() { return sk; }
    public void setSk(String sk) { this.sk = sk; }
    
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    // DynamoDB helper methods
    public static String buildPartitionKey(String schoolId, String studentId) {
        return "SCHOOL#" + schoolId + "#STUDENT#" + studentId;
    }
    
    public static String buildSortKey() {
        return "#METADATA";
    }
    
    public void generateKeys() {
        String schoolCode = getSchool() != null ? getSchool().getSchoolCode() : null;
        String studentId = getStudentId();
        if (schoolCode != null && studentId != null) {
            this.pk = buildPartitionKey(schoolCode, studentId);
            this.sk = buildSortKey();
        }
    }
    
    public static final TableSchema<Student> TABLE_SCHEMA = TableSchema.fromBean(Student.class);
}