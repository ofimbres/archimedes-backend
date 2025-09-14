package com.binomiaux.archimedes.service.jpa;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.entity.Enrollment;
import com.binomiaux.archimedes.entity.Period;

/**
 * JPA version of PeriodRegistrationService for managing period registration codes.
 * Implements stateless registration code system using JPA entities only.
 */
@Service
public class PeriodRegistrationJpaService {

    private final PeriodJpaService periodJpaService;
    private final EnrollmentJpaService enrollmentJpaService;

    public PeriodRegistrationJpaService(PeriodJpaService periodJpaService, EnrollmentJpaService enrollmentJpaService) {
        this.periodJpaService = periodJpaService;
        this.enrollmentJpaService = enrollmentJpaService;
    }

    /**
     * Generates a registration code for a period.
     * Format: [SCHOOL_CODE][PERIOD_ID_SUFFIX][CHECKSUM]
     * Example: SCH1P001456
     * 
     * @param schoolCode School code (e.g., "SCH1")
     * @param periodId Period ID (e.g., "PER-12345678")
     * @return Registration code
     */
    public String generateRegistrationCode(String schoolCode, String periodId) {
        // Validate inputs
        if (schoolCode == null || periodId == null) {
            throw new IllegalArgumentException("School code and Period ID are required");
        }
        
        // Ensure period exists
        Period period = periodJpaService.getPeriodByPeriodId(periodId);
        if (period == null || !period.getSchool().getSchoolCode().equals(schoolCode)) {
            throw new IllegalArgumentException("Period " + periodId + " not found in school " + schoolCode);
        }

        // Create a shorter representation for the code
        String periodSuffix = extractPeriodSuffix(periodId);
        String base = schoolCode + periodSuffix;
        String checksum = generateChecksum(base);
        return base + checksum;
    }

    /**
     * Validates and uses a registration code to enroll a student.
     * 
     * @param code Registration code
     * @param studentId Student ID attempting to join
     * @return EnrollmentResult with success/failure details
     */
    public EnrollmentResult useRegistrationCode(String code, String studentId) {
        // Parse and validate code format
        PeriodInfo periodInfo = parseAndValidateCode(code);
        if (periodInfo == null) {
            return EnrollmentResult.error("Invalid registration code format");
        }

        // Reconstruct full period ID
        String fullPeriodId = reconstructPeriodId(periodInfo.getPeriodSuffix());
        
        // Verify period still exists and is active
        try {
            Period period = periodJpaService.getPeriodByPeriodId(fullPeriodId);
            
            if (!period.getSchool().getSchoolCode().equals(periodInfo.getSchoolCode())) {
                return EnrollmentResult.error("Registration code is not valid for this period");
            }
            
            if (!"ACTIVE".equals(period.getStatus())) {
                return EnrollmentResult.error("Period is not currently active");
            }

            // Check if student is already enrolled
            if (enrollmentJpaService.isStudentEnrolledInPeriod(studentId, fullPeriodId)) {
                return EnrollmentResult.error("Student is already enrolled in this period");
            }

            // Enroll student  
            Enrollment enrollment = enrollmentJpaService.enrollStudent(studentId, fullPeriodId);
            return EnrollmentResult.success(enrollment);
            
        } catch (Exception e) {
            return EnrollmentResult.error("Failed to enroll student: " + e.getMessage());
        }
    }

    /**
     * Validates a registration code without using it.
     * 
     * @param code Registration code to validate
     * @return PeriodInfo if valid, null if invalid
     */
    public PeriodInfo validateRegistrationCode(String code) {
        return parseAndValidateCode(code);
    }

    /**
     * Parses and validates a registration code.
     * 
     * @param code Registration code
     * @return PeriodInfo if valid, null if invalid
     */
    private PeriodInfo parseAndValidateCode(String code) {
        if (code == null || code.length() < 9) { // Minimum: 4 + 4 + 3
            return null;
        }

        try {
            // Extract components (assuming 4-char school code and 4-char period suffix)
            String schoolCode = code.substring(0, 4);
            String periodSuffix = code.substring(4, 8);
            String providedChecksum = code.substring(8);

            // Validate format
            if (!isValidSchoolCode(schoolCode) || !isValidPeriodSuffix(periodSuffix)) {
                return null;
            }

            // Validate checksum
            String expectedChecksum = generateChecksum(schoolCode + periodSuffix);
            if (!expectedChecksum.equals(providedChecksum)) {
                return null; // Invalid checksum
            }

            return new PeriodInfo(schoolCode, periodSuffix);
        } catch (Exception e) {
            return null; // Any parsing error means invalid code
        }
    }

    /**
     * Generates a 3-character checksum for the given input.
     * 
     * @param input Input string to generate checksum for
     * @return 3-character checksum
     */
    private String generateChecksum(String input) {
        int hash = Math.abs(input.hashCode());
        return String.format("%03d", hash % 1000);
    }

    /**
     * Extract a 4-character suffix from period ID for use in registration codes
     */
    private String extractPeriodSuffix(String periodId) {
        // For period ID "PER-12345678", extract last 4 characters of the UUID part
        if (periodId.startsWith("PER-")) {
            String uuidPart = periodId.substring(4);
            return "P" + uuidPart.substring(uuidPart.length() - 3);
        }
        return "P001"; // Default fallback
    }

    /**
     * Reconstruct period ID from suffix (this would need a lookup in real implementation)
     * For now, this is a simplified approach
     */
    private String reconstructPeriodId(String periodSuffix) {
        // In a real implementation, you'd need a lookup table or search
        // For now, we'll just return it as-is and let the service handle the lookup
        return periodSuffix;
    }

    /**
     * Validates school code format (4 characters).
     */
    private boolean isValidSchoolCode(String schoolCode) {
        return schoolCode != null && schoolCode.length() == 4 && Pattern.matches("[A-Z0-9]{4}", schoolCode);
    }

    /**
     * Validates period suffix format (P followed by 3 characters).
     */
    private boolean isValidPeriodSuffix(String periodSuffix) {
        return periodSuffix != null && Pattern.matches("P[A-Z0-9]{3}", periodSuffix);
    }

    /**
     * Result of registration code usage.
     */
    public static class EnrollmentResult {
        private final boolean success;
        private final String errorMessage;
        private final Enrollment enrollment;

        private EnrollmentResult(boolean success, String errorMessage, Enrollment enrollment) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.enrollment = enrollment;
        }

        public static EnrollmentResult success(Enrollment enrollment) {
            return new EnrollmentResult(true, null, enrollment);
        }

        public static EnrollmentResult error(String message) {
            return new EnrollmentResult(false, message, null);
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public Enrollment getEnrollment() { return enrollment; }
    }

    /**
     * Information extracted from a registration code.
     */
    public static class PeriodInfo {
        private final String schoolCode;
        private final String periodSuffix;

        public PeriodInfo(String schoolCode, String periodSuffix) {
            this.schoolCode = schoolCode;
            this.periodSuffix = periodSuffix;
        }

        public String getSchoolCode() { return schoolCode; }
        public String getPeriodSuffix() { return periodSuffix; }
    }
}