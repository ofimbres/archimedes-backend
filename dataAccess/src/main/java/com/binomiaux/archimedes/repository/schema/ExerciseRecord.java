package com.binomiaux.archimedes.repository.schema;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class ExerciseRecord {
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

    public static final TableSchema<ExerciseRecord> TABLE_SCHEMA = TableSchema.fromBean(ExerciseRecord.class);
}