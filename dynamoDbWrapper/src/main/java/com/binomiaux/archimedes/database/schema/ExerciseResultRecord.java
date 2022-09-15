package com.binomiaux.archimedes.database.schema;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.Data;

@DynamoDBTable(tableName = "ArchimedesData")
@Data
public class ExerciseResultRecord {
    @DynamoDBHashKey(attributeName="pk")
    private String pk;
    @DynamoDBRangeKey(attributeName="sk")
    private String sk;
    @DynamoDBAttribute(attributeName="studentName")
    private String studentName;
    @DynamoDBAttribute(attributeName="exerciseCode")
    private String exerciseCode;
    @DynamoDBAttribute(attributeName="exerciseName")
    private String exerciseName;
    @DynamoDBAttribute(attributeName="timestamp")
    private String timestamp;
    @DynamoDBAttribute(attributeName="score")
    private int score;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "gsi1", attributeName = "gsipk")
    private String gsipk;
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "gsi1", attributeName = "gsisk")
    private String gsisk;
}
