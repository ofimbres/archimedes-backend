package com.binomiaux.archimedes.repository;

import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.config.aws.DynamoDbProperties;
import com.binomiaux.archimedes.model.Student;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

/**
 * Simple StudentRepository using merged Student model.
 * Direct approach without over-engineering - Student handles both business and persistence.
 */
@Repository
public class StudentRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbProperties dynamoDbProperties;

    public StudentRepository(DynamoDbEnhancedClient enhancedClient, DynamoDbProperties dynamoDbProperties) {
        this.enhancedClient = enhancedClient;
        this.dynamoDbProperties = dynamoDbProperties;
    }

    private DynamoDbTable<Student> getTable() {
        return enhancedClient.table(dynamoDbProperties.getTableName(), Student.TABLE_SCHEMA);
    }

    public Student find(String schoolId, String studentId) {
        Key key = Key.builder()
                .partitionValue(Student.buildPartitionKey(schoolId, studentId))
                .sortValue(Student.buildSortKey())
                .build();
                
        return getTable().getItem(key);
    }

    public void create(Student student) {
        // Use the Student's built-in key generation for consistency
        student.generateKeys();
        getTable().putItem(student);
    }

    public void update(Student student) {
        // Use the Student's built-in key generation for consistency
        student.generateKeys();
        getTable().updateItem(student);
    }

    public void delete(String schoolId, String studentId) {
        Key key = Key.builder()
                .partitionValue(Student.buildPartitionKey(schoolId, studentId))
                .sortValue(Student.buildSortKey())
                .build();
                
        getTable().deleteItem(key);
    }
}
