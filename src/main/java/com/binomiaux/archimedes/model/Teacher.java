package com.binomiaux.archimedes.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Compatibility bridge for DynamoDB repositories.
 * Extends JPA entity but adds DynamoDB-specific functionality.
 */
@DynamoDbBean
public class Teacher extends com.binomiaux.archimedes.entity.Teacher {
    
    // DynamoDB-specific fields
    private String pk;
    private String sk;
    private String entityType;
    private String schoolIdField; // backup field for DynamoDB compatibility
    private String fullName;
    private String username;
    private Integer maxPeriods;
    private String parentEntityKey;
    private String childEntityKey;
    private String searchTypeKey;
    
    public Teacher() {
        super();
    }
    
    public Teacher(String teacherId, com.binomiaux.archimedes.entity.School school, String firstName, String lastName) {
        super(teacherId, school, firstName, lastName);
        this.entityType = "TEACHER";
    }
    
    // DynamoDB key methods
    public String getPk() { return pk; }
    public void setPk(String pk) { this.pk = pk; }
    
    public String getSk() { return sk; }
    public void setSk(String sk) { this.sk = sk; }
    
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    // DynamoDB-specific getters/setters
    public String getSchoolId() { 
        return getSchool() != null ? getSchool().getSchoolCode() : schoolIdField; 
    }
    public void setSchoolId(String schoolId) { 
        this.schoolIdField = schoolId; 
    }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public Integer getMaxPeriods() { return maxPeriods; }
    public void setMaxPeriods(int maxPeriods) { this.maxPeriods = maxPeriods; }
    
    public String getParentEntityKey() { return parentEntityKey; }
    public void setParentEntityKey(String parentEntityKey) { this.parentEntityKey = parentEntityKey; }
    
    public String getChildEntityKey() { return childEntityKey; }
    public void setChildEntityKey(String childEntityKey) { this.childEntityKey = childEntityKey; }
    
    public String getSearchTypeKey() { return searchTypeKey; }
    public void setSearchTypeKey(String searchTypeKey) { this.searchTypeKey = searchTypeKey; }
    
    // DynamoDB helper methods
    public static String buildPartitionKey(String schoolId, String teacherId) {
        return "SCHOOL#" + schoolId + "#TEACHER#" + teacherId;
    }
    
    public static String buildSortKey() {
        return "#METADATA";
    }
    
    public void generateKeys() {
        String schoolCode = getSchoolId();
        String teacherId = getTeacherId();
        if (schoolCode != null && teacherId != null) {
            this.pk = buildPartitionKey(schoolCode, teacherId);
            this.sk = buildSortKey();
            this.parentEntityKey = "SCHOOL#" + schoolCode;
            this.childEntityKey = "TEACHER#" + teacherId;
            this.searchTypeKey = "TEACHER_TYPE";
        }
    }
    
    public static final TableSchema<Teacher> TABLE_SCHEMA = TableSchema.fromBean(Teacher.class);
}