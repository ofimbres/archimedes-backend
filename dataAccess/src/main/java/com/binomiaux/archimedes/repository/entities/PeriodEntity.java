package com.binomiaux.archimedes.repository.entities;

import java.util.List;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class PeriodEntity {
    private String pk;
    private String sk;
    private String type;
    private String id;
    private String schoolCode;
    private String teacherCode;
    private String periodCode;
    private String name;
    private List<String> attendedBy;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
    }

    public String getPeriodCode() {
        return periodCode;
    }

    public void setPeriodCode(String periodCode) {
        this.periodCode = periodCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAttendedBy(List<String> attendedBy) {
        this.attendedBy = attendedBy;
    }   

    public List<String> getAttendedBy() {
        return attendedBy;
    }

    public static final TableSchema<PeriodEntity> TABLE_SCHEMA = TableSchema.fromBean(PeriodEntity.class);
}
