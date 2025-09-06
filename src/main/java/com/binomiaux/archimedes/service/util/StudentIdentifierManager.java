package com.binomiaux.archimedes.service.util;

/**
 * Utility for managing both database IDs and user-friendly identifiers.
 * Database uses full hierarchical IDs, users see shorter, memorable alternatives.
 */
public class StudentIdentifierManager {

    /**
     * Generates the primary database student ID (full format).
     * This is used internally for database storage and queries.
     * 
     * @param schoolNumber the full school number (e.g., "240901042")
     * @param studentSequence always 1 since one student per school
     * @return database student ID (e.g., "240901042-001")
     */
    public static String generateDatabaseStudentId(String schoolNumber, int studentSequence) {
        if (schoolNumber == null || studentSequence < 1) {
            throw new IllegalArgumentException("Invalid school number or sequence");
        }
        
        return String.format("%s-%03d", schoolNumber, studentSequence);
    }

    /**
     * Generates a user-friendly student display ID.
     * This is what students see in the UI and use for login.
     * 
     * @param schoolNumber the full school number
     * @return user-friendly ID (e.g., "STU24042" or "S24042")
     */
    public static String generateUserFriendlyId(String schoolNumber) {
        if (schoolNumber == null || schoolNumber.length() < 5) {
            throw new IllegalArgumentException("Invalid school number");
        }
        
        // Take last 5 digits and prefix with "S"
        String shortId = schoolNumber.substring(schoolNumber.length() - 5);
        return "S" + shortId; // e.g., "S01042"
    }

    /**
     * Generates a memorable username for the student.
     * Could be based on school info + sequence, or completely custom.
     * 
     * @param schoolName the school name (e.g., "Lamar Middle")
     * @param studentSequence the student sequence (always 1)
     * @return memorable username (e.g., "lamar.student")
     */
    public static String generateUsername(String schoolName, int studentSequence) {
        if (schoolName == null || schoolName.isEmpty()) {
            throw new IllegalArgumentException("School name cannot be null or empty");
        }
        
        // Clean school name: take first word, lowercase, add suffix
        String cleanName = schoolName.toLowerCase()
                .split("\\s+")[0] // Take first word
                .replaceAll("[^a-z0-9]", ""); // Remove special chars
        
        return cleanName + ".student"; // e.g., "lamar.student"
    }

    /**
     * Alternative: Generate a simple numeric user ID.
     * 
     * @param schoolNumber the full school number
     * @return simple 4-6 digit user ID (e.g., "24042")
     */
    public static String generateSimpleUserId(String schoolNumber) {
        if (schoolNumber == null || schoolNumber.length() < 5) {
            throw new IllegalArgumentException("Invalid school number");
        }
        
        // Hash to get a consistent but shorter number
        int hash = Math.abs(schoolNumber.hashCode());
        return String.format("%05d", hash % 100000);
    }

    /**
     * Maps a user-friendly ID back to the database ID.
     * Useful for login/authentication flows.
     * 
     * @param userFriendlyId the display ID (e.g., "S01042")
     * @param schoolNumber the full school number for validation
     * @return database ID (e.g., "240901042-001")
     */
    public static String mapToDatabaseId(String userFriendlyId, String schoolNumber) {
        if (userFriendlyId == null || schoolNumber == null) {
            throw new IllegalArgumentException("IDs cannot be null");
        }
        
        // Validate the mapping is correct
        String expectedUserFriendly = generateUserFriendlyId(schoolNumber);
        if (!expectedUserFriendly.equals(userFriendlyId)) {
            throw new IllegalArgumentException("User ID does not match school");
        }
        
        return generateDatabaseStudentId(schoolNumber, 1);
    }
}
