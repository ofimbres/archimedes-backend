package com.binomiaux.archimedes.repository;

import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.config.aws.DynamoDbProperties;
import com.binomiaux.archimedes.model.Teacher;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

/**
 * Simple TeacherRepository using merged Teacher model.
 * Direct approach without over-engineering - Teacher handles both business and persistence.
 */
@Repository
public class TeacherRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbProperties dynamoDbProperties;

    public TeacherRepository(DynamoDbEnhancedClient enhancedClient, DynamoDbProperties dynamoDbProperties) {
        this.enhancedClient = enhancedClient;
        this.dynamoDbProperties = dynamoDbProperties;
    }

    private DynamoDbTable<Teacher> getTable() {
        return enhancedClient.table(dynamoDbProperties.getTableName(), Teacher.TABLE_SCHEMA);
    }

    public Teacher find(String schoolId, String teacherId) {
        Key key = Key.builder()
                .partitionValue(Teacher.buildPartitionKey(schoolId, teacherId))
                .sortValue(Teacher.buildSortKey())
                .build();
                
        return getTable().getItem(key);
    }

    public void create(Teacher teacher) {
        // Use the Teacher's built-in key generation for consistency
        teacher.generateKeys();
        getTable().putItem(teacher);
    }

    public void update(Teacher teacher) {
        // Use the Teacher's built-in key generation for consistency
        teacher.generateKeys();
        getTable().updateItem(teacher);
    }

    public void delete(String schoolId, String teacherId) {
        Key key = Key.builder()
                .partitionValue(Teacher.buildPartitionKey(schoolId, teacherId))
                .sortValue(Teacher.buildSortKey())
                .build();
                
        getTable().deleteItem(key);
    }
}