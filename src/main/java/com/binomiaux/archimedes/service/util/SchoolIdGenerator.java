package com.binomiaux.archimedes.service.util;

/**
 * Utility for generating school IDs from external school database records.
 * Creates 4-5 character unique identifiers following the pattern: [County][SchoolSuffix]
 */
public class SchoolIdGenerator {

    /**
     * Generates a 5-character school ID from database record.
     * Format: [3-digit county][2-digit school suffix]
     * 
     * @param countyNumber the county number (e.g., "240")
     * @param schoolNumber the full school number (e.g., "240901042")
     * @return 5-character school ID (e.g., "24042")
     */
    public static String generateSchoolId(String countyNumber, String schoolNumber) {
        if (countyNumber == null || schoolNumber == null) {
            throw new IllegalArgumentException("County number and school number cannot be null");
        }
        
        // Pad county to 3 digits if needed
        String paddedCounty = String.format("%03d", Integer.parseInt(countyNumber));
        
        // Take last 2 digits of school number
        String schoolSuffix = schoolNumber.substring(Math.max(0, schoolNumber.length() - 2));
        
        return paddedCounty + schoolSuffix;
    }

    /**
     * Generates a 4-character school ID using district-based approach.
     * Format: [3-digit district prefix][1-digit sequence]
     * 
     * @param districtNumber the district number (e.g., "240901")
     * @param sequenceInDistrict sequence number within district (0-9)
     * @return 4-character school ID (e.g., "2401")
     */
    public static String generateCompactSchoolId(String districtNumber, int sequenceInDistrict) {
        if (districtNumber == null || sequenceInDistrict < 0 || sequenceInDistrict > 9) {
            throw new IllegalArgumentException("Invalid district number or sequence");
        }
        
        // Take first 3 digits of district
        String districtPrefix = districtNumber.substring(0, Math.min(3, districtNumber.length()));
        
        return String.format("%s%d", districtPrefix, sequenceInDistrict);
    }

    /**
     * Generates a collision-resistant school ID using the full school number.
     * This is the safest approach to avoid duplicates.
     * 
     * @param fullSchoolNumber the complete school number (e.g., "240901042")
     * @return the school ID (same as input for uniqueness)
     */
    public static String generateSafeSchoolId(String fullSchoolNumber) {
        if (fullSchoolNumber == null || fullSchoolNumber.isEmpty()) {
            throw new IllegalArgumentException("School number cannot be null or empty");
        }
        return fullSchoolNumber; // Use full number to guarantee uniqueness
    }

    /**
     * Generates a hash-based school ID for shorter IDs with low collision risk.
     * 
     * @param fullSchoolNumber the complete school identifier
     * @return 6-character alphanumeric ID (e.g., "A7B2C4")
     */
    public static String generateHashedSchoolId(String fullSchoolNumber) {
        if (fullSchoolNumber == null || fullSchoolNumber.isEmpty()) {
            throw new IllegalArgumentException("School number cannot be null or empty");
        }
        
        // Use a more robust hash with larger space to reduce collisions
        long hash = Math.abs((long) fullSchoolNumber.hashCode());
        return String.format("%06X", hash % 16777216).substring(0, 6); // 6 hex chars = ~16M possibilities
    }

    /**
     * Generates a district-aware school ID with lower collision risk.
     * Format: [4-digit district][3-digit school suffix]
     * 
     * @param districtNumber the district number (e.g., "240901")
     * @param schoolNumber the full school number (e.g., "240901042")
     * @return 7-character school ID (e.g., "2409042")
     */
    public static String generateDistrictSchoolId(String districtNumber, String schoolNumber) {
        if (districtNumber == null || schoolNumber == null) {
            throw new IllegalArgumentException("District and school numbers cannot be null");
        }
        
        // Take first 4 digits of district
        String districtPart = districtNumber.substring(0, Math.min(4, districtNumber.length()));
        
        // Take last 3 digits of school number
        String schoolPart = schoolNumber.substring(Math.max(0, schoolNumber.length() - 3));
        
        return districtPart + schoolPart;
    }
}
