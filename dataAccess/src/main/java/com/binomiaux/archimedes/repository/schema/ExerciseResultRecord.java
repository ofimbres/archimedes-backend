package com.binomiaux.archimedes.repository.schema;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class ExerciseResultRecord {
    private String pk;
    private String sk;
    private String firstName;
    private String lastName;
    private String exerciseId;
    private String exerciseName;
    private String timestamp;
    private int score;
    private String s3Key;
    private String type;
    private String gsipk;
    private String gsisk;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
    
    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi1")
    public String getGsipk() {
        return gsipk;
    }

    public void setGsipk(String gsipk) {
        this.gsipk = gsipk;
    }

    @DynamoDbSecondarySortKey(indexNames = "gsi1")
    public String getGsisk() {
        return gsisk;
    }

    public void setGsisk(String gsisk) {
        this.gsisk = gsisk;
    }

    public static final TableSchema<ExerciseResultRecord> TABLE_SCHEMA = TableSchema.fromBean(ExerciseResultRecord.class);
}