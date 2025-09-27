package com.binomiaux.archimedes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * Entity to track sequential user ID counters per school and user type.
 * Ensures clean, sequential IDs like STU-001, STU-002, TCH-001, etc.
 */
@Entity
@Table(name = "user_id_counters")
@IdClass(UserIdCounterKey.class)
public class UserIdCounter {

    @Id
    @Column(name = "school_id")
    private Long schoolId;

    @Id
    @Column(name = "user_type")
    private String userType;

    @Column(name = "next_sequence")
    private Integer nextSequence = 1;

    // Constructors
    public UserIdCounter() {}

    public UserIdCounter(Long schoolId, String userType) {
        this.schoolId = schoolId;
        this.userType = userType;
        this.nextSequence = 1;
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

    public Integer getNextSequence() {
        return nextSequence;
    }

    public void setNextSequence(Integer nextSequence) {
        this.nextSequence = nextSequence;
    }

    /**
     * Get the next sequence number and increment the counter.
     */
    public Integer getAndIncrement() {
        Integer current = this.nextSequence;
        this.nextSequence++;
        return current;
    }
}