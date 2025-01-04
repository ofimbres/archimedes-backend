package com.binomiaux.archimedes.repository.entities;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class ActivitySubmissionEntity {
    private String pk;
    private String sk;
    private String activityResultId;
    private String firstName;
    private String lastName;
    private String activityId;
    private String activityName;
    private String timestamp;
    private int score;
    private String path;
    private String type;
    private String gsi1pk;
    private String gsi1sk;
    private String gsi2pk;
    private String gsi2sk;

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

    public String getActivityResultId() {
        return activityResultId;
    }

    public void setActivityResultId(String activityResultId) {
        this.activityResultId = activityResultId;
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

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
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
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi1")
    public String getGsi1pk() {
		return gsi1pk;
	}

	public void setGsi1pk(String gsi1pk) {
		this.gsi1pk = gsi1pk;
	}

    @DynamoDbSecondarySortKey(indexNames = "gsi1")
    public String getGsi1sk() {
		return gsi1sk;
	}

	public void setGsi1sk(String gsi1sk) {
		this.gsi1sk = gsi1sk;
	}

    @DynamoDbSecondaryPartitionKey(indexNames = "gsi2")
    public String getGsi2pk() {
        return gsi2pk;
    }

    public void setGsi2pk(String gsi2pk) {
        this.gsi2pk = gsi2pk;
    }

    @DynamoDbSecondarySortKey(indexNames = "gsi2")
    public String getGsi2sk() {
        return gsi2sk;
    }

    public void setGsi2sk(String gsi2sk) {
        this.gsi2sk = gsi2sk;
    }

    public static final TableSchema<ActivitySubmissionEntity> TABLE_SCHEMA = TableSchema.fromBean(ActivitySubmissionEntity.class);
}