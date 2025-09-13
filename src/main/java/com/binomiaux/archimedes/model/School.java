package com.binomiaux.archimedes.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * School entity representing educational institutions.
 * Uses DynamoDB single-table design with proper key structure.
 */
@DynamoDbBean
public class School {
    
    // DynamoDB keys
    private String pk;                       // SCHOOL#SCH001
    private String sk;                       // #METADATA
    private String type;                     // SCHOOL (entity type)
    
    // GSI keys for efficient querying
    private String parentEntityKey;          // "SCHOOLS" (GSI1PK) - for getting all schools
    private String childEntityKey;           // SCHOOL#SCH001 (GSI1SK)
    private String searchTypeKey;            // "SCHOOL_CODE" (GSI2PK) - for searching by school code
    private String searchValueKey;           // actual school code (GSI2SK)
    
    // Core school fields
    private String id;                       // SCH001
    private String schoolCode;               // SCHOOL001 (unique identifier)
    private String name;                     // "Lincoln High School"
    private String address;                  // "123 Main St, City, State"
    private String principalName;            // "Dr. Jane Smith"
    private String contactEmail;             // "contact@lincolnhigh.edu"
    private String phoneNumber;              // "+1-555-123-4567"
    
    // Aggregated counters (denormalized for performance)
    private int studentCount;                // Number of students
    private int teacherCount;                // Number of teachers
    private int periodCount;                 // Number of active periods

    // Constructors
    public School() {}

    public School(String id, String schoolCode, String name) {
        this.id = id;
        this.schoolCode = schoolCode;
        this.name = name;
        this.type = "SCHOOL";
        this.studentCount = 0;
        this.teacherCount = 0;
        this.periodCount = 0;
        
        // Set GSI keys for efficient querying
        this.parentEntityKey = "SCHOOLS";  // All schools share this GSI1PK
        this.childEntityKey = "SCHOOL#" + id;
        this.searchTypeKey = "SCHOOL_CODE";
        this.searchValueKey = schoolCode;
    }

    // DynamoDB annotations
    @DynamoDbPartitionKey
    @DynamoDbAttribute("pk")
    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("sk")
    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

        // GSI1 - For querying all schools
    @DynamoDbSecondaryPartitionKey(indexNames = "gsi1")
    @DynamoDbAttribute("gsi1pk")
    public String getParentEntityKey() {
        return parentEntityKey;
    }

    public void setParentEntityKey(String parentEntityKey) {
        this.parentEntityKey = parentEntityKey;
    }

    @DynamoDbSecondarySortKey(indexNames = "gsi1")
    @DynamoDbAttribute("gsi1sk")
    public String getChildEntityKey() {
        return childEntityKey;
    }

    public void setChildEntityKey(String childEntityKey) {
        this.childEntityKey = childEntityKey;
    }

    // GSI2 - For querying schools by school code
    @DynamoDbSecondaryPartitionKey(indexNames = "gsi2")
    @DynamoDbAttribute("gsi2pk")
    public String getSearchTypeKey() {
        return searchTypeKey;
    }

    public void setSearchTypeKey(String searchTypeKey) {
        this.searchTypeKey = searchTypeKey;
    }

    @DynamoDbSecondarySortKey(indexNames = "gsi2")
    @DynamoDbAttribute("gsi2sk")
    public String getSearchValueKey() {
        return searchValueKey;
    }

    public void setSearchValueKey(String searchValueKey) {
        this.searchValueKey = searchValueKey;
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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


    public static final TableSchema<School> TABLE_SCHEMA = TableSchema.fromBean(School.class);
}
