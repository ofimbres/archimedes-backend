package com.binomiaux.archimedes.repository.schema;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.Data;

@DynamoDBTable(tableName = "ArchimedesData")
@Data
public class ExerciseResultRecord {
    @DynamoDBHashKey(attributeName="pk")
    private String pk;
    @DynamoDBRangeKey(attributeName="sk")
    private String sk;
    @DynamoDBAttribute(attributeName="firstName")
    private String firstName;
    @DynamoDBAttribute(attributeName="lastName")
    private String lastName;
    @DynamoDBAttribute(attributeName="exerciseId")
    private String exerciseId;
    @DynamoDBAttribute(attributeName="exerciseName")
    private String exerciseName;
    @DynamoDBAttribute(attributeName="timestamp")
    private String timestamp;
    @DynamoDBAttribute(attributeName="score")
    private int score;
    @DynamoDBAttribute(attributeName="s3Key")
    private String s3Key;

    @DynamoDBAttribute(attributeName = "type")
    private String type;
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "gsi1", attributeName = "gsipk")
    private String gsipk;
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "gsi1", attributeName = "gsisk")
    private String gsisk;
}
