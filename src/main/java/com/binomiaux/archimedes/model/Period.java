package com.binomiaux.archimedes.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Compatibility bridge for DynamoDB repositories.
 * Extends JPA entity but adds DynamoDB-specific functionality.
 */
@DynamoDbBean
public class Period extends com.binomiaux.archimedes.entity.Period {
    
    // DynamoDB-specific fields
    private String pk;
    private String sk;
    private String entityType;
    private String teacherFirstName;
    private String teacherLastName;
    private String teacherFullName;
    private String schoolIdField; // to avoid conflict with JPA school relationship
    private String teacherIdField; // to avoid conflict with JPA teacher relationship
    
    public Period() {
        super();
    }
    
    public Period(String periodId, com.binomiaux.archimedes.entity.School school, com.binomiaux.archimedes.entity.Teacher teacher, String name) {
        super(periodId, school, teacher, name);
        this.entityType = "PERIOD";
    }
    
    // DynamoDB key methods
    public String getPk() { return pk; }
    public void setPk(String pk) { this.pk = pk; }
    
    public String getSk() { return sk; }
    public void setSk(String sk) { this.sk = sk; }
    
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    // DynamoDB convenience getters that map to JPA relationships
    public String getSchoolId() { 
        return getSchool() != null ? getSchool().getSchoolCode() : schoolIdField; 
    }
    public void setSchoolId(String schoolId) { 
        this.schoolIdField = schoolId; 
    }
    
    public String getTeacherId() { 
        return getTeacher() != null ? getTeacher().getTeacherId() : teacherIdField; 
    }
    public void setTeacherId(String teacherId) { 
        this.teacherIdField = teacherId; 
    }
    
    // DynamoDB denormalized teacher fields
    public String getTeacherFirstName() { return teacherFirstName; }
    public void setTeacherFirstName(String teacherFirstName) { this.teacherFirstName = teacherFirstName; }
    
    public String getTeacherLastName() { return teacherLastName; }
    public void setTeacherLastName(String teacherLastName) { this.teacherLastName = teacherLastName; }
    
    public String getTeacherFullName() { return teacherFullName; }
    public void setTeacherFullName(String teacherFullName) { this.teacherFullName = teacherFullName; }
    
    // DynamoDB helper methods
    public static String buildPartitionKey(String schoolId, String periodId) {
        return "SCHOOL#" + schoolId + "#PERIOD#" + periodId;
    }
    
    public static String buildSortKey() {
        return "#METADATA";
    }
    
    public void generateKeys() {
        String schoolId = getSchoolId();
        String periodId = getPeriodId();
        if (schoolId != null && periodId != null) {
            this.pk = buildPartitionKey(schoolId, periodId);
            this.sk = buildSortKey();
        }
    }
    
    public static final TableSchema<Period> TABLE_SCHEMA = TableSchema.fromBean(Period.class);
}