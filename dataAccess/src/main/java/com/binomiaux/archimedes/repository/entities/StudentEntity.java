package com.binomiaux.archimedes.repository.entities;

import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.List;

@DynamoDbBean
public class StudentEntity {
    private String pk;
    private String sk;
    private String type;
    private String code;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String schoolCode;
    private List<Attends> attends;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public List<Attends> getAttends() {
        return attends;
    }

    public void setAttends(List<Attends> attends) {
        this.attends = attends;
    }

    public static final TableSchema<StudentEntity> TABLE_SCHEMA = TableSchema.fromBean(StudentEntity.class);

    @DynamoDbBean
    public static class Attends {
        public String teacherCode;
        public String periodCode;

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
    }
}
