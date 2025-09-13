package com.binomiaux.archimedes.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Teacher entity with improved DynamoDB design and cleaner field naming.
 */
@DynamoDbBean
public class Teacher {
    
    // DynamoDB keys
    private String pk;                       // TEACHER#SCH001#guadalupe.trevino
    private String sk;                       // #METADATA
    private String entityType;              // TEACHER
    private String parentEntityKey;         // SCHOOL#SCH001 (GSI1PK)
    private String childEntityKey;          // TEACHER#SCH001#guadalupe.trevino (GSI1SK)
    private String searchTypeKey;           // EMAIL (GSI2PK)
    private String searchValueKey;          // guadalupe.trevino@gmail.com (GSI2SK)
    
    // Core teacher fields
    private String teacherId;               // guadalupe.trevino (or TCH001, etc.)
    private String schoolId;                // SCH001
    private String firstName;               // "Guadalupe"
    private String lastName;                // "Trevino"
    private String fullName;                // "Guadalupe Trevino" (computed for display)
    private String email;                   // "guadalupe.trevino@gmail.com"
    private String username;                // "guadalupe.trevino"
    private Integer maxPeriods;             // 6

    // Constructors
    public Teacher() {}

    public Teacher(String teacherId, String schoolId, String firstName, String lastName, 
                   String email, String username) {
        this.teacherId = teacherId;
        this.schoolId = schoolId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.maxPeriods = 6; // Default value
        
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

    // GSI1 - For querying teachers by school
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

    // GSI2 - For querying teachers by email
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

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
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

    public Integer getMaxPeriods() {
        return maxPeriods;
    }

    public void setMaxPeriods(Integer maxPeriods) {
        this.maxPeriods = maxPeriods;
    }

    // Legacy method support (will be removed)
    @Deprecated
    public String getId() {
        return teacherId;
    }

    @Deprecated
    public void setId(String id) {
        this.teacherId = id;
    }

    @Deprecated
    public String getType() {
        return entityType;
    }

    @Deprecated
    public void setType(String type) {
        this.entityType = type;
    }

    // Helper methods for key generation
    public void generateKeys() {
        this.pk = "TEACHER#" + this.schoolId + "#" + this.teacherId;  // TEACHER#SCH001#guadalupe.trevino
        this.sk = "#METADATA";
        this.childEntityKey = "TEACHER#" + this.schoolId + "#" + this.teacherId;  // TEACHER#SCH001#guadalupe.trevino
        this.parentEntityKey = "SCHOOL#" + this.schoolId;
        this.searchTypeKey = "EMAIL";
        this.searchValueKey = this.email;
        this.fullName = this.firstName + " " + this.lastName;
        this.entityType = "TEACHER";
    }

    // Static helper for repository queries
    public static String buildPartitionKey(String schoolId, String teacherId) {
        return "TEACHER#" + schoolId + "#" + teacherId;
    }

    public static String buildSortKey() {
        return "#METADATA";
    }

    public static final TableSchema<Teacher> TABLE_SCHEMA = TableSchema.fromBean(Teacher.class);
}
