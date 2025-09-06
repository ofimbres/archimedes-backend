package com.binomiaux.archimedes.service.util;

/**
 * Utility for generating student IDs based on school identifiers.
 * Creates hierarchical student IDs following the pattern: [SchoolId]-[StudentSequence]
 */
public class StudentIdGenerator {

    /**
     * Generates a student ID using full school number format.
     * Format: [Full School Number]-[3-digit sequence]
     * 
     * @param schoolNumber the full school number (e.g., "240901042")
     * @param studentSequence the student sequence number within school (1-999)
     * @return student ID (e.g., "240901042-001")
     */
    public static String generateFullStudentId(String schoolNumber, int studentSequence) {
        if (schoolNumber == null || studentSequence < 1 || studentSequence > 999) {
            throw new IllegalArgumentException("Invalid school number or student sequence (must be 1-999)");
        }
        
        return String.format("%s-%03d", schoolNumber, studentSequence);
    }

    /**
     * Generates a student ID using shortened school ID format (Recommended).
     * Format: [5-digit School ID]-[3-digit sequence]
     * 
     * @param shortSchoolId the 5-character school ID (e.g., "24042")
     * @param studentSequence the student sequence number within school (1-999)
     * @return student ID (e.g., "24042-001")
     */
    public static String generateStudentId(String shortSchoolId, int studentSequence) {
        if (shortSchoolId == null || shortSchoolId.length() != 5 || studentSequence < 1 || studentSequence > 999) {
            throw new IllegalArgumentException("Invalid school ID (must be 5 chars) or student sequence (must be 1-999)");
        }
        
        return String.format("%s-%03d", shortSchoolId, studentSequence);
    }

    /**
     * Generates a compact student ID without separator.
     * Format: [5-digit School ID][3-digit sequence]
     * 
     * @param shortSchoolId the 5-character school ID (e.g., "24042")
     * @param studentSequence the student sequence number within school (1-999)
     * @return compact student ID (e.g., "24042001")
     */
    public static String generateCompactStudentId(String shortSchoolId, int studentSequence) {
        if (shortSchoolId == null || shortSchoolId.length() != 5 || studentSequence < 1 || studentSequence > 999) {
            throw new IllegalArgumentException("Invalid school ID (must be 5 chars) or student sequence (must be 1-999)");
        }
        
        return String.format("%s%03d", shortSchoolId, studentSequence);
    }

    /**
     * Generates a student ID with support for larger schools (up to 9999 students).
     * Format: [5-digit School ID]-[4-digit sequence]
     * 
     * @param shortSchoolId the 5-character school ID (e.g., "24042")
     * @param studentSequence the student sequence number within school (1-9999)
     * @return student ID (e.g., "24042-0001")
     */
    public static String generateLargeSchoolStudentId(String shortSchoolId, int studentSequence) {
        if (shortSchoolId == null || shortSchoolId.length() != 5 || studentSequence < 1 || studentSequence > 9999) {
            throw new IllegalArgumentException("Invalid school ID (must be 5 chars) or student sequence (must be 1-9999)");
        }
        
        return String.format("%s-%04d", shortSchoolId, studentSequence);
    }

    /**
     * Extracts the school ID from a student ID.
     * 
     * @param studentId the student ID (e.g., "24042-001")
     * @return the school ID portion (e.g., "24042")
     */
    public static String extractSchoolId(String studentId) {
        if (studentId == null || !studentId.contains("-")) {
            throw new IllegalArgumentException("Invalid student ID format");
        }
        
        return studentId.split("-")[0];
    }

    /**
     * Extracts the student sequence from a student ID.
     * 
     * @param studentId the student ID (e.g., "24042-001")
     * @return the student sequence number (e.g., 1)
     */
    public static int extractStudentSequence(String studentId) {
        if (studentId == null || !studentId.contains("-")) {
            throw new IllegalArgumentException("Invalid student ID format");
        }
        
        String[] parts = studentId.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid student ID format");
        }
        
        return Integer.parseInt(parts[1]);
    }
}
