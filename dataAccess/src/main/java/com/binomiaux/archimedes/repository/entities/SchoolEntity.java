package com.binomiaux.archimedes.repository.entities;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class SchoolEntity {
    private String pk;
    private String sk;
    private String type;
    private String id;
    private String schoolCode;
    private int studentCount;
    private int teacherCount;

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

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentsCount) {
        this.studentCount = studentsCount;
    }

    public int getTeacherCount() {
        return teacherCount;
    }

    public void setTeacherCount(int teachersCount) {
        this.teacherCount = teachersCount;
    }


    public static final TableSchema<SchoolEntity> TABLE_SCHEMA = TableSchema.fromBean(SchoolEntity.class);
}
