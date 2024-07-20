package com.binomiaux.archimedes.repository.entities;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class TeacherEntity {
    private String pk;
    private String sk;
    private String type;
    private String id;
    private String schoolCode;
    private String teacherCode;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private int maxPeriods;

    public TeacherEntity() {
    }

    public TeacherEntity(String pk, String sk, String type, String id, String schoolCode, String teacherCode, String firstName, String lastName,
            String username, String email) {
        this.pk = pk;
        this.sk = sk;
        this.type = type;
        this.id = id;
        this.schoolCode = schoolCode;
        this.teacherCode = teacherCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getMaxPeriods() {
        return maxPeriods;
    }

    public void setMaxPeriods(int maxPeriods) {
        this.maxPeriods = maxPeriods;
    }

    public static final TableSchema<TeacherEntity> TABLE_SCHEMA = TableSchema.fromBean(TeacherEntity.class);
}
