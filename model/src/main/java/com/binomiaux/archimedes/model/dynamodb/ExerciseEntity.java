package com.binomiaux.archimedes.model.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;


@DynamoDbBean
public class ExerciseEntity {
    public static final TableSchema<ExerciseEntity> TABLE_SCHEMA = TableSchema.fromBean(ExerciseEntity.class);

    private String pk;
    private String sk;
    private String code;
    private String name;
    private String classification;
    private String path;
    private String gsi1pk;
    private String gsi1sk;
    private String gsi2pk;
    private String gsi2sk;

    public ExerciseEntity() {
    }

    public ExerciseEntity(String pk, String sk, String code, String name, String classification, String path,
            String gsi1pk, String gsi1sk, String gsi2pk, String gsi2sk) {
        this.pk = pk;
        this.sk = sk;
        this.code = code;
        this.name = name;
        this.classification = classification;
        this.path = path;
        this.gsi1pk = gsi1pk;
        this.gsi1sk = gsi1sk;
        this.gsi2pk = gsi2pk;
        this.gsi2sk = gsi2sk;
    }

    @DynamoDbPartitionKey 
    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @DynamoDbSortKey 
    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi1") 
    public String getGsipk() {
        return gsi1pk;
    }

    public void setGsipk(String gsi1pk) {
        this.gsi1pk = gsi1pk;
    }

    @DynamoDbSecondarySortKey(indexNames = "gsi1") 
    public String getGsisk() {
        return gsi1sk;
    }

    public void setGsisk(String gsi1sk) {
        this.gsi1sk = gsi1sk;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi2") 
    public String getGsipk2() {
        return gsi2pk;
    }

    public void setGsipk2(String gsi2pk) {
        this.gsi2pk = gsi2pk;
    }

    @DynamoDbSecondarySortKey(indexNames = "gsi2") 
    public String getGsisk2() {
        return gsi2sk;
    }

    public void setGsisk2(String gsi2sk) {
        this.gsi2sk = gsi2sk;
    }    
}