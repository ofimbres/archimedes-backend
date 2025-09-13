package com.binomiaux.archimedes.model;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Counter entity for managing sequential ID generation.
 * Implements the Counter schema design from the DynamoDB documentation.
 */
@DynamoDbBean
public class Counter {

    public static final TableSchema<Counter> TABLE_SCHEMA = TableSchema.fromBean(Counter.class);

    private String pk;
    private String sk;
    private String entityType;
    private Integer teacherCounter;
    private Integer studentCounter;
    private Integer periodCounter;
    private Integer enrollmentCounter;
    private String lastUpdated;
    
    // GSI attributes (null for counters as per schema)
    private String gsi1PK;
    private String gsi1SK;
    private String gsi2PK;
    private String gsi2SK;

    // Default constructor for DynamoDB
    public Counter() {}

    // Partition Key
    @DynamoDbPartitionKey
    @DynamoDbAttribute("pk")
    public String getPk() { return pk; }
    public void setPk(String pk) { this.pk = pk; }

    // Sort Key
    @DynamoDbSortKey
    @DynamoDbAttribute("sk")
    public String getSk() { return sk; }
    public void setSk(String sk) { this.sk = sk; }

    // Entity Type
    @DynamoDbAttribute("entityType")
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    // Counter fields
    @DynamoDbAttribute("teacherCounter")
    public Integer getTeacherCounter() { return teacherCounter; }
    public void setTeacherCounter(Integer teacherCounter) { this.teacherCounter = teacherCounter; }

    @DynamoDbAttribute("studentCounter")
    public Integer getStudentCounter() { return studentCounter; }
    public void setStudentCounter(Integer studentCounter) { this.studentCounter = studentCounter; }

    @DynamoDbAttribute("periodCounter")
    public Integer getPeriodCounter() { return periodCounter; }
    public void setPeriodCounter(Integer periodCounter) { this.periodCounter = periodCounter; }

    @DynamoDbAttribute("enrollmentCounter")
    public Integer getEnrollmentCounter() { return enrollmentCounter; }
    public void setEnrollmentCounter(Integer enrollmentCounter) { this.enrollmentCounter = enrollmentCounter; }

    @DynamoDbAttribute("lastUpdated")
    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    // GSI1 attributes (null for counters)
    @DynamoDbSecondaryPartitionKey(indexNames = "GSI1")
    @DynamoDbAttribute("gsi1PK")
    public String getGsi1PK() { return gsi1PK; }
    public void setGsi1PK(String gsi1PK) { this.gsi1PK = gsi1PK; }

    @DynamoDbSecondarySortKey(indexNames = "GSI1")
    @DynamoDbAttribute("gsi1SK")
    public String getGsi1SK() { return gsi1SK; }
    public void setGsi1SK(String gsi1SK) { this.gsi1SK = gsi1SK; }

    // GSI2 attributes (null for counters)
    @DynamoDbSecondaryPartitionKey(indexNames = "GSI2")
    @DynamoDbAttribute("gsi2PK")
    public String getGsi2PK() { return gsi2PK; }
    public void setGsi2PK(String gsi2PK) { this.gsi2PK = gsi2PK; }

    @DynamoDbSecondarySortKey(indexNames = "GSI2")
    @DynamoDbAttribute("gsi2SK")
    public String getGsi2SK() { return gsi2SK; }
    public void setGsi2SK(String gsi2SK) { this.gsi2SK = gsi2SK; }

    @Override
    public String toString() {
        return "Counter{" +
                "pk='" + pk + '\'' +
                ", sk='" + sk + '\'' +
                ", entityType='" + entityType + '\'' +
                ", teacherCounter=" + teacherCounter +
                ", studentCounter=" + studentCounter +
                ", periodCounter=" + periodCounter +
                ", enrollmentCounter=" + enrollmentCounter +
                ", lastUpdated='" + lastUpdated + '\'' +
                '}';
    }
}
