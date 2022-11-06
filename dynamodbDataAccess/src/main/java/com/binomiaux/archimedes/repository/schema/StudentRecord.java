package com.binomiaux.archimedes.repository.schema;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.Data;

@DynamoDBTable(tableName = "ArchimedesData")
@Data
public class StudentRecord {
    @DynamoDBHashKey(attributeName="pk")
    private String pk;
    @DynamoDBRangeKey(attributeName="sk")
    private String sk;
    @DynamoDBAttribute(attributeName="firstName")
    private String firstName;
    @DynamoDBAttribute(attributeName="lastName")
    private String lastName;
    @DynamoDBAttribute(attributeName="username")
    private String username;
    @DynamoDBAttribute(attributeName="email")
    private String email;
}
