package com.binomiaux.archimedes.repository.schema;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Data;

@DynamoDBTable(tableName = "ArchimedesData")
@Data
public class ExerciseRecord {
    @DynamoDBHashKey(attributeName="pk")
    private String pk;
    @DynamoDBRangeKey(attributeName="sk")
    private String sk;
    @DynamoDBAttribute(attributeName="code")
    private String code;
    @DynamoDBAttribute(attributeName="name")
    private String name;
    @DynamoDBAttribute(attributeName="classification")
    private String classification;
    @DynamoDBAttribute(attributeName="path")
    private String path;
}
