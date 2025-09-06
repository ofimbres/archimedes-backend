package com.binomiaux.archimedes.util;

/**
 * Helper utility for DynamoDB key operations
 * Handles extraction and formatting of keys with entity prefixes
 */
public class DynamoKeyHelper {
    
    public static final String SCHOOL_PREFIX = "SCHOOL#";
    public static final String STUDENT_PREFIX = "STUDENT#";
    public static final String TEACHER_PREFIX = "TEACHER#";
    public static final String PERIOD_PREFIX = "PERIOD#";
    public static final String ACTIVITY_PREFIX = "ACTIVITY#";
    public static final String TOPIC_PREFIX = "TOPIC#";
    
    /**
     * Creates a school partition key
     * @param schoolNumber the school number (e.g., "240901042")
     * @return formatted partition key (e.g., "SCHOOL#240901042")
     */
    public static String createSchoolPk(String schoolNumber) {
        return SCHOOL_PREFIX + schoolNumber;
    }
    
    /**
     * Extracts school number from partition key
     * @param pk the partition key (e.g., "SCHOOL#240901042")
     * @return clean school number (e.g., "240901042")
     */
    public static String extractSchoolNumber(String pk) {
        if (pk == null || !pk.startsWith(SCHOOL_PREFIX)) {
            return null;
        }
        return pk.substring(SCHOOL_PREFIX.length());
    }
    
    /**
     * Creates a student partition key
     * @param studentId the student ID (e.g., "240901042-001")
     * @return formatted partition key (e.g., "STUDENT#240901042-001")
     */
    public static String createStudentPk(String studentId) {
        return STUDENT_PREFIX + studentId;
    }
    
    /**
     * Extracts student ID from partition key
     * @param pk the partition key (e.g., "STUDENT#240901042-001")
     * @return clean student ID (e.g., "240901042-001")
     */
    public static String extractStudentId(String pk) {
        if (pk == null || !pk.startsWith(STUDENT_PREFIX)) {
            return null;
        }
        return pk.substring(STUDENT_PREFIX.length());
    }
    
    /**
     * Creates an activity sort key
     * @param activityId the activity ID (e.g., "ACT001")
     * @return formatted sort key (e.g., "ACTIVITY#ACT001")
     */
    public static String createActivitySk(String activityId) {
        return ACTIVITY_PREFIX + activityId;
    }
    
    /**
     * Extracts activity ID from sort key
     * @param sk the sort key (e.g., "ACTIVITY#ACT001")
     * @return clean activity ID (e.g., "ACT001")
     */
    public static String extractActivityId(String sk) {
        if (sk == null || !sk.startsWith(ACTIVITY_PREFIX)) {
            return null;
        }
        return sk.substring(ACTIVITY_PREFIX.length());
    }
    
    /**
     * Creates a topic partition key
     * @param topic the topic name (e.g., "MATH")
     * @param subtopic the subtopic name (e.g., "ALGEBRA") - optional
     * @return formatted partition key (e.g., "TOPIC#MATH#SUBTOPIC#ALGEBRA")
     */
    public static String createTopicPk(String topic, String subtopic) {
        if (subtopic != null && !subtopic.trim().isEmpty()) {
            return TOPIC_PREFIX + topic.toUpperCase() + "#SUBTOPIC#" + subtopic.toUpperCase();
        }
        return TOPIC_PREFIX + topic.toUpperCase();
    }
    
    /**
     * Creates GSI2PK for root topic queries
     * @return "ROOT_TOPIC" constant for querying all root topics
     */
    public static String createRootTopicGsi2Pk() {
        return "ROOT_TOPIC";
    }
    
    /**
     * Extracts topic ID from partition key
     * @param pk the partition key (e.g., "TOPIC#ALGEBRAIC_EXPRESSIONS")
     * @return clean topic ID (e.g., "ALGEBRAIC_EXPRESSIONS")
     */
    public static String extractTopicId(String pk) {
        if (pk == null || !pk.startsWith(TOPIC_PREFIX)) {
            return null;
        }
        String remaining = pk.substring(TOPIC_PREFIX.length());
        // Handle subtopic case: TOPIC#ALGEBRAIC_EXPRESSIONS#SUBTOPIC#AX1
        if (remaining.contains("#SUBTOPIC#")) {
            return remaining.substring(0, remaining.indexOf("#SUBTOPIC#"));
        }
        return remaining;
    }
    
    /**
     * Determines entity type from partition key
     * @param pk the partition key
     * @return entity type or "UNKNOWN"
     */
    public static String getEntityType(String pk) {
        if (pk == null) return "UNKNOWN";
        
        if (pk.startsWith(SCHOOL_PREFIX)) return "SCHOOL";
        if (pk.startsWith(STUDENT_PREFIX)) return "STUDENT";
        if (pk.startsWith(TEACHER_PREFIX)) return "TEACHER";
        if (pk.startsWith(PERIOD_PREFIX)) return "PERIOD";
        if (pk.startsWith(TOPIC_PREFIX)) return "TOPIC";
        
        return "UNKNOWN";
    }
}
