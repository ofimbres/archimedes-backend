package com.binomiaux.archimedes.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key class for UserIdCounter entity.
 */
public class UserIdCounterKey implements Serializable {

    private Long schoolId;
    private String userType;

    // Constructors
    public UserIdCounterKey() {}

    public UserIdCounterKey(Long schoolId, String userType) {
        this.schoolId = schoolId;
        this.userType = userType;
    }

    // Getters and Setters
    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserIdCounterKey that = (UserIdCounterKey) o;
        return Objects.equals(schoolId, that.schoolId) && 
               Objects.equals(userType, that.userType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schoolId, userType);
    }
}