package com.binomiaux.archimedes.repository;

import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.config.aws.DynamoDbProperties;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.repository.util.DynamoKeyBuilder;

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

    public Student find(String studentId) {
        Key key = Key.builder()
                .partitionValue(DynamoKeyBuilder.buildStudentKey(studentId))
                .sortValue("STUDENT")
                .build();
                
        return getTable().getItem(key);
    }

    public void create(Student student) {
        // Set DynamoDB keys
        student.setPk(DynamoKeyBuilder.buildStudentKey(student.getStudentId()));
        student.setSk("STUDENT");
        student.setEntityType("STUDENT");
        
        // Set GSI keys for querying
        student.setParentEntityKey(DynamoKeyBuilder.buildSchoolKey(student.getSchoolId()));
        student.setChildEntityKey("STUDENT#" + student.getStudentId());
        
        if (student.getUsername() != null) {
            student.setSearchTypeKey("USERNAME");
            student.setSearchValueKey(student.getUsername());
        }
        
        getTable().putItem(student);
    }

    public void update(Student student) {
        // Ensure keys are set
        student.setPk(DynamoKeyBuilder.buildStudentKey(student.getStudentId()));
        student.setSk("STUDENT");
        
        getTable().updateItem(student);
    }

    public void delete(String studentId) {
        Key key = Key.builder()
                .partitionValue(DynamoKeyBuilder.buildStudentKey(studentId))
                .sortValue("STUDENT")
                .build();
                
        getTable().deleteItem(key);
    }
}
