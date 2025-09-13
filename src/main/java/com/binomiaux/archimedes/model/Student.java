package com.binomiaux.archimedes.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Student entity with improved DynamoDB design and denormalized display data.
 */
@DynamoDbBean
public class Student {
    
    // DynamoDB keys
    private String pk;                       // STUDENT#SCH001#diego.hinojosa
    private String sk;                       // #METADATA
    private String entityType;              // STUDENT
    private String parentEntityKey;         // SCHOOL#SCH001 (GSI1PK)
    private String childEntityKey;          // STUDENT#SCH001#diego.hinojosa (GSI1SK)
    private String searchTypeKey;           // EMAIL (GSI2PK)
    private String searchValueKey;          // diego.hinojosa@gmail.com (GSI2SK)
    
    // Core student fields
    private String studentId;               // diego.hinojosa (or STU001, etc.)
    private String schoolId;                // SCH001
    private String firstName;               // "Diego"
    private String lastName;                // "Hinojosa"
    private String fullName;                // "Diego Hinojosa" (computed for display)
    private String email;                   // "diego.hinojosa@gmail.com"
    private String username;                // "diego.hinojosa"

    // Constructors
    public Student() {}

    public Student(String studentId, String schoolId, String firstName, String lastName, String email, String username) {
        this.studentId = studentId;
        this.schoolId = schoolId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        
        // Generate all computed fields and keys
        generateKeys();
    }

    // DynamoDB Primary Key
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

    // GSI1 - For querying students by school
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

    // GSI2 - For querying students by email
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

    // Entity fields
    @DynamoDbAttribute("entityType")
    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        // Update computed fullName when firstName changes
        if (this.lastName != null) {
            this.fullName = firstName + " " + this.lastName;
        }
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        // Update computed fullName when lastName changes
        if (this.firstName != null) {
            this.fullName = this.firstName + " " + lastName;
        }
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Helper methods for key generation
    public void generateKeys() {
        this.pk = "STUDENT#" + this.schoolId + "#" + this.studentId;  // STUDENT#SCH001#diego.hinojosa
        this.sk = "#METADATA";
        this.childEntityKey = "STUDENT#" + this.schoolId + "#" + this.studentId;  // STUDENT#SCH001#diego.hinojosa
        this.parentEntityKey = "SCHOOL#" + this.schoolId;
        this.searchTypeKey = "EMAIL";
        this.searchValueKey = this.email;
        this.fullName = this.firstName + " " + this.lastName;
        this.entityType = "STUDENT";
    }

    // Static helper for repository queries
    public static String buildPartitionKey(String schoolId, String studentId) {
        return "STUDENT#" + schoolId + "#" + studentId;
    }

    public static String buildSortKey() {
        return "#METADATA";
    }

    public static final TableSchema<Student> TABLE_SCHEMA = TableSchema.fromBean(Student.class);
}
