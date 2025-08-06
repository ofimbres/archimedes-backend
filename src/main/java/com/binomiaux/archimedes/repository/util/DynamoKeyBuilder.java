package com.binomiaux.archimedes.repository.util;

import org.springframework.stereotype.Component;

/**
 * Utility class for building DynamoDB partition and sort keys consistently.
 * This centralizes key generation logic and reduces duplication across repositories.
 */
@Component
public class DynamoKeyBuilder {
    
    // Constants
    public static final String METADATA_KEY = "#METADATA";
    
    // Simplified Key builders - using consistent naming
    public static String buildStudentKey(String studentId) {
        return "STUDENT#" + studentId;
    }
    
    public static String buildTeacherKey(String teacherId) {
        return "TEACHER#" + teacherId;
    }
    
    public static String buildSchoolKey(String schoolId) {
        return "SCHOOL#" + schoolId;
    }
    
    public static String buildPeriodKey(String periodId) {
        return "PERIOD#" + periodId;
    }
    
    public static String buildActivityKey(String activityId) {
        return "ACTIVITY#" + activityId;
    }
    
    public static String buildTopicKey(String topicId) {
        return "TOPIC#" + topicId;
    }
    
    // Additional composite keys
    public static String buildActivityResultKey(String activityResultId) {
        return "ACTIVITY_RESULT#" + activityResultId;
    }
    
    public static String buildPeriodStudentKey(String periodId, String studentId) {
        return "PERIOD#" + periodId + "#STUDENT#" + studentId;
    }
    
    public static String buildDateKey(String date) {
        return "DATE#" + date;
    }
    
    public static String buildNameKey(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
    
    public static String buildTopicSubtopicKey(String topicId, String subtopicId) {
        return "TOPIC#" + topicId + "#SUBTOPIC#" + subtopicId;
    }
    
    // Legacy methods for backward compatibility
    public static String buildStudentPK(String studentId) {
        return "STUDENT#" + studentId;
    }
    
    public static String buildTeacherPK(String teacherId) {
        return "TEACHER#" + teacherId;
    }
    
    public static String buildSchoolPK(String schoolId) {
        return "SCHOOL#" + schoolId;
    }
    
    public static String buildPeriodPK(String periodId) {
        return "PERIOD#" + periodId;
    }
    
    public static String buildActivityPK(String activityId) {
        return "ACTIVITY#" + activityId;
    }
    
    public static String buildTopicPK(String topicId) {
        return "TOPIC#" + topicId;
    }
    
    // Sort Key builders
    public static String buildMetadataSK() {
        return "#METADATA";
    }
    
    public static String buildStudentSK(String studentId) {
        return "STUDENT#" + studentId;
    }
    
    public static String buildActivitySK(String activityId) {
        return "ACTIVITY#" + activityId;
    }
    
    // GSI Key builders
    public static String buildSchoolGSI1PK(String schoolId) {
        return "SCHOOL#" + schoolId;
    }
    
    public static String buildStudentGSI1SK(String studentId) {
        return "STUDENT#" + studentId;
    }
    
    public static String buildTeacherGSI1SK(String teacherId) {
        return "TEACHER#" + teacherId;
    }
    
    public static String buildPeriodGSI1SK(String periodId) {
        return "PERIOD#" + periodId;
    }
    
    public static String buildNameGSI2SK(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
}
