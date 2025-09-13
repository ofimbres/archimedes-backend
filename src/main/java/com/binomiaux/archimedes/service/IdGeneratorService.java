package com.binomiaux.archimedes.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.config.aws.DynamoDbProperties;
import com.binomiaux.archimedes.model.Counter;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

/**
 * Service for generating sequential IDs using DynamoDB atomic counters.
 * Implements the ID generation strategy from the schema design.
 */
@Service
public class IdGeneratorService {

    private final DynamoDbEnhancedClient dynamoDbClient;
    private final DynamoDbProperties dynamoDbProperties;
    private final DynamoDbTable<Counter> countersTable;

    @Autowired
    public IdGeneratorService(DynamoDbEnhancedClient dynamoDbClient, DynamoDbProperties dynamoDbProperties) {
        this.dynamoDbClient = dynamoDbClient;
        this.dynamoDbProperties = dynamoDbProperties;
        this.countersTable = dynamoDbClient.table(dynamoDbProperties.getTableName(), Counter.TABLE_SCHEMA);
    }

    /**
     * Generates a sequential teacher ID (T001, T002, etc.)
     * 
     * @param schoolId School ID to generate teacher ID for
     * @return Sequential teacher ID
     */
    public String generateTeacherId(String schoolId) {
        return generateId(schoolId, "teacher", "T");
    }

    /**
     * Generates a sequential student ID (S001, S002, etc.)
     * 
     * @param schoolId School ID to generate student ID for
     * @return Sequential student ID
     */
    public String generateStudentId(String schoolId) {
        return generateId(schoolId, "student", "S");
    }

    /**
     * Generates a sequential period ID (P001, P002, etc.)
     * 
     * @param schoolId School ID to generate period ID for
     * @return Sequential period ID
     */
    public String generatePeriodId(String schoolId) {
        return generateId(schoolId, "period", "P");
    }

    /**
     * Generates a sequential enrollment ID (E001, E002, etc.)
     * 
     * @param schoolId School ID to generate enrollment ID for
     * @return Sequential enrollment ID
     */
    public String generateEnrollmentId(String schoolId) {
        return generateId(schoolId, "enrollment", "E");
    }

    /**
     * Test method to check if we can connect to DynamoDB and what the table structure is
     */
    public void testDynamoDbConnection() {
        try {
            System.out.println("Testing DynamoDB connection...");
            System.out.println("Table name: " + dynamoDbProperties.getTableName());
            
            // Try to scan the table with limit 1 to see if it exists
            var scanRequest = countersTable.scan(builder -> builder.limit(1));
            var itemsList = scanRequest.items().stream().toList();
            
            System.out.println("Table exists and scan returned " + itemsList.size() + " items");
            if (!itemsList.isEmpty()) {
                Counter item = itemsList.get(0);
                System.out.println("Sample item: PK=" + item.getPk() + ", SK=" + item.getSk());
            }
            
        } catch (Exception e) {
            System.err.println("DynamoDB connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initializes counters for a new school.
     * Should be called when a new school is created.
     * 
     * @param schoolId School ID to initialize counters for
     */
    public void initializeSchoolCounters(String schoolId) {
        Counter counter = new Counter();
        counter.setPk("COUNTER#SCHOOL#" + schoolId);
        counter.setSk("#METADATA");
        counter.setEntityType("COUNTER");
        counter.setTeacherCounter(0);
        counter.setStudentCounter(0);
        counter.setPeriodCounter(0);
        counter.setEnrollmentCounter(0);
        counter.setLastUpdated(Instant.now().toString());
        
        // GSI values are null for counters as per schema
        counter.setGsi1PK(null);
        counter.setGsi1SK(null);
        counter.setGsi2PK(null);
        counter.setGsi2SK(null);

        countersTable.putItem(counter);
    }

    /**
     * Generic method to generate sequential IDs using atomic counters.
     * 
     * @param schoolId School ID
     * @param type Counter type (teacher, student, period, enrollment)
     * @param prefix ID prefix (T, S, P, E)
     * @return Sequential ID with format PREFIX###
     */
    private String generateId(String schoolId, String type, String prefix) {
        String counterKey = "COUNTER#SCHOOL#" + schoolId;
        
        try {
            System.out.println("Generating " + type + " ID for school: " + schoolId);
            System.out.println("Table name: " + dynamoDbProperties.getTableName());
            System.out.println("Counter PK: " + counterKey);
            System.out.println("Counter SK: #METADATA");
            
            // Get current counter record
            Key key = Key.builder()
                .partitionValue(counterKey)
                .sortValue("#METADATA")
                .build();
            
            System.out.println("Key object: " + key);
            
            Counter counter = countersTable.getItem(key);
            System.out.println("Counter found: " + (counter != null));
            
            // If counter doesn't exist, initialize it
            if (counter == null) {
                System.out.println("Initializing counters for school: " + schoolId);
                initializeSchoolCounters(schoolId);
                counter = countersTable.getItem(key);
                System.out.println("Counter after initialization: " + (counter != null));
            }
            
            if (counter == null) {
                throw new RuntimeException("Failed to initialize or retrieve counter for school: " + schoolId);
            }
            
            // Increment the appropriate counter
            int newValue = incrementCounter(counter, type);
            counter.setLastUpdated(Instant.now().toString());
            
            System.out.println("New " + type + " counter value: " + newValue);
            
            // Update the record
            countersTable.updateItem(counter);
            
            String generatedId = String.format("%s%03d", prefix, newValue);
            System.out.println("Generated ID: " + generatedId);
            return generatedId;
            
        } catch (Exception e) {
            // Fallback to UUID if counter operation fails
            String fallbackId = prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            System.err.println("Counter operation failed for " + type + " in school " + schoolId + 
                             ". Using fallback ID: " + fallbackId + ". Error: " + e.getMessage());
            e.printStackTrace();
            return fallbackId;
        }
    }

    /**
     * Increments the specified counter type and returns the new value.
     * 
     * @param counter Counter object to modify
     * @param type Counter type to increment
     * @return New counter value
     */
    private int incrementCounter(Counter counter, String type) {
        switch (type) {
            case "teacher":
                int teacherCount = (counter.getTeacherCounter() != null ? counter.getTeacherCounter() : 0) + 1;
                counter.setTeacherCounter(teacherCount);
                return teacherCount;
            case "student":
                int studentCount = (counter.getStudentCounter() != null ? counter.getStudentCounter() : 0) + 1;
                counter.setStudentCounter(studentCount);
                return studentCount;
            case "period":
                int periodCount = (counter.getPeriodCounter() != null ? counter.getPeriodCounter() : 0) + 1;
                counter.setPeriodCounter(periodCount);
                return periodCount;
            case "enrollment":
                int enrollmentCount = (counter.getEnrollmentCounter() != null ? counter.getEnrollmentCounter() : 0) + 1;
                counter.setEnrollmentCounter(enrollmentCount);
                return enrollmentCount;
            default:
                throw new IllegalArgumentException("Unknown counter type: " + type);
        }
    }

    /**
     * Extracts the counter value from the result based on type.
     * 
     * @param counter Counter object returned from update
     * @param type Counter type
     * @return Counter value
     */
    private int getCounterValue(Counter counter, String type) {
        switch (type) {
            case "teacher": return counter.getTeacherCounter();
            case "student": return counter.getStudentCounter();
            case "period": return counter.getPeriodCounter();
            case "enrollment": return counter.getEnrollmentCounter();
            default: return 0;
        }
    }
}
