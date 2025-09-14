package com.binomiaux.archimedes.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Compatibility bridge for DynamoDB repositories.
 * Extends JPA entity but adds DynamoDB-specific functionality.
 */
@DynamoDbBean  
public class School extends com.binomiaux.archimedes.entity.School {
    
    // DynamoDB-specific fields
    private String pk;
    private String sk;
    private String type;
    private String parentEntityKey;
    private String childEntityKey;
    private String searchTypeKey;
    private String searchValueKey;
    private Integer studentCount;
    private Integer teacherCount;
    private Integer periodCount;
    
    public School() {
        super();
    }
    
    public School(String schoolCode, String name) {
        super(schoolCode, name);
    }
    
    public School(String schoolCode, String name, String address) {
        this(schoolCode, name);
        setAddress(address);
    }
    
    // DynamoDB key methods
    public String getPk() { return pk; }
    public void setPk(String pk) { this.pk = pk; }
    
    public String getSk() { return sk; }
    public void setSk(String sk) { this.sk = sk; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    // DynamoDB GSI fields
    public String getParentEntityKey() { return parentEntityKey; }
    public void setParentEntityKey(String parentEntityKey) { this.parentEntityKey = parentEntityKey; }
    
    public String getChildEntityKey() { return childEntityKey; }
    public void setChildEntityKey(String childEntityKey) { this.childEntityKey = childEntityKey; }
    
    public String getSearchTypeKey() { return searchTypeKey; }
    public void setSearchTypeKey(String searchTypeKey) { this.searchTypeKey = searchTypeKey; }
    
    public String getSearchValueKey() { return searchValueKey; }
    public void setSearchValueKey(String searchValueKey) { this.searchValueKey = searchValueKey; }
    
    // DynamoDB counters
    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
    
    public Integer getTeacherCount() { return teacherCount; }
    public void setTeacherCount(Integer teacherCount) { this.teacherCount = teacherCount; }
    
    public Integer getPeriodCount() { return periodCount; }
    public void setPeriodCount(Integer periodCount) { this.periodCount = periodCount; }
    
    public static final TableSchema<School> TABLE_SCHEMA = TableSchema.fromBean(School.class);
}