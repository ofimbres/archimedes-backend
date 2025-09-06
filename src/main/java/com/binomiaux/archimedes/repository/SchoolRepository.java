package com.binomiaux.archimedes.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.config.aws.DynamoDbProperties;
import com.binomiaux.archimedes.model.School;
import com.binomiaux.archimedes.repository.util.DynamoKeyBuilder;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

/**
 * Repository for School entity operations using DynamoDB.
 * Follows the single-table design pattern with proper key management.
 */
@Repository
public class SchoolRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbProperties dynamoDbProperties;

    public SchoolRepository(DynamoDbEnhancedClient enhancedClient, DynamoDbProperties dynamoDbProperties) {
        this.enhancedClient = enhancedClient;
        this.dynamoDbProperties = dynamoDbProperties;
    }

    private DynamoDbTable<School> getTable() {
        return enhancedClient.table(dynamoDbProperties.getTableName(), School.TABLE_SCHEMA);
    }

    /**
     * Find a school by its ID.
     * 
     * @param schoolId the school identifier
     * @return the School entity or null if not found
     */
    public School findById(String schoolId) {
        Key key = Key.builder()
                .partitionValue(DynamoKeyBuilder.buildSchoolKey(schoolId))
                .sortValue("#METADATA")
                .build();
                
        return getTable().getItem(key);
    }

    /**
     * Find a school by its school code using GSI2.
     * Uses GSI2 where GSI2PK = "SCHOOL_CODE" and GSI2SK = schoolCode
     * 
     * @param schoolCode the school code
     * @return the School entity or null if not found
     */
    public School findBySchoolCode(String schoolCode) {
        // Use GSI2 for efficient lookup by school code
        // GSI2PK = "SCHOOL_CODE", GSI2SK = actual school code value
        DynamoDbIndex<School> gsi2 = getTable().index("GSI2");
        
        Key gsi2Key = Key.builder()
                .partitionValue("SCHOOL_CODE")
                .sortValue(schoolCode)
                .build();

        return gsi2.query(QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(gsi2Key))
                .limit(1)
                .build())
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all schools using GSI1.
     * Uses GSI1 where GSI1PK = "SCHOOLS" to get all school entities
     * Note: In production, implement pagination for large datasets
     * 
     * @return list of all schools
     */
    public List<School> findAll() {
        // Use GSI1 for efficient lookup of all schools
        // GSI1PK = "SCHOOLS" for all school entities
        DynamoDbIndex<School> gsi1 = getTable().index("GSI1");
        
        Key gsi1Key = Key.builder()
                .partitionValue("SCHOOLS")
                .build();

        return gsi1.query(QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(gsi1Key))
                .build())
                .stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }

    /**
     * Create a new school.
     * 
     * @param school the school to create
     */
    public void create(School school) {
        // Set DynamoDB keys following the pattern
        school.setPk(DynamoKeyBuilder.buildSchoolKey(school.getId()));
        school.setSk("#METADATA");
        school.setType("SCHOOL");
        
        // Set GSI keys for efficient querying
        school.setParentEntityKey("SCHOOLS");  // GSI1PK - for getting all schools
        school.setChildEntityKey("SCHOOL#" + school.getId()); // GSI1SK
        school.setSearchTypeKey("SCHOOL_CODE"); // GSI2PK - for searching by school code
        school.setSearchValueKey(school.getSchoolCode()); // GSI2SK
        
        getTable().putItem(school);
    }

    /**
     * Update an existing school.
     * 
     * @param school the school to update
     */
    public void update(School school) {
        // Ensure GSI keys are properly set for updates
        if (school.getParentEntityKey() == null) {
            school.setParentEntityKey("SCHOOLS");
        }
        if (school.getChildEntityKey() == null) {
            school.setChildEntityKey("SCHOOL#" + school.getId());
        }
        if (school.getSearchTypeKey() == null) {
            school.setSearchTypeKey("SCHOOL_CODE");
        }
        if (school.getSearchValueKey() == null) {
            school.setSearchValueKey(school.getSchoolCode());
        }
        
        getTable().putItem(school);
    }

    /**
     * Delete a school by ID.
     * 
     * @param schoolId the school identifier
     */
    public void delete(String schoolId) {
        Key key = Key.builder()
                .partitionValue(DynamoKeyBuilder.buildSchoolKey(schoolId))
                .sortValue("#METADATA")
                .build();
                
        getTable().deleteItem(key);
    }
}
