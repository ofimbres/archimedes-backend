package com.binomiaux.archimedes;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Data;

//@DynamoDBTable(tableName = "archimedes-exercises")
@Data
public class Exercise {

    private String name;
    private String code;
    private String type;
    private String sk;
    //private String indexName;
    private String s3Location;
}
