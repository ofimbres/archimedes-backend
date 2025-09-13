package com.binomiaux.archimedes.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.Enrollment;

import java.util.regex.Pattern;

/**
 * Service for managing period registration codes.
 * Implements stateless registration code system as per schema design.
 */
@Service
public class PeriodRegistrationService {

    private final PeriodService periodService;
    private final PeriodEnrollmentService enrollmentService;

    @Autowired
    public PeriodRegistrationService(PeriodService periodService, PeriodEnrollmentService enrollmentService) {
        this.periodService = periodService;
        this.enrollmentService = enrollmentService;
    }

    /**
     * Generates a registration code for a period.
     * Format: [SCHOOL_ID][PERIOD_ID][CHECKSUM]
     * Example: 1234P001456
     * 
     * @param schoolId School ID (4 digits)
     * @param compositePeriodId Composite Period ID (e.g., SCH001#TCH001#PER001)
     * @return Registration code
     */
    public String generateRegistrationCode(String schoolId, String compositePeriodId) {
        // Validate inputs
        if (schoolId == null || compositePeriodId == null) {
            throw new IllegalArgumentException("School ID and Period ID are required");
        }
        
        // Ensure period exists
        Period period = periodService.getPeriod(compositePeriodId);
        if (period == null || !period.getSchoolId().equals(schoolId)) {
            throw new IllegalArgumentException("Period " + compositePeriodId + " not found in school " + schoolId);
        }

        String base = schoolId + compositePeriodId;
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

        // Verify period still exists and is active
        Period period = periodService.findPeriodInSchool(periodInfo.getSchoolId(), periodInfo.getPeriodId());
        if (period == null) {
            return EnrollmentResult.error("Period no longer exists");
        }
        
        if (!period.getSchoolId().equals(periodInfo.getSchoolId())) {
            return EnrollmentResult.error("Registration code is not valid for this period");
        }

        // Check if student is already enrolled
        String simplifiedPeriodId = period.getTeacherId() + "-" + periodInfo.getPeriodId();
        if (enrollmentService.isStudentEnrolled(periodInfo.getSchoolId(), studentId, simplifiedPeriodId)) {
            return EnrollmentResult.error("Student is already enrolled in this period");
        }

        // Enroll student  
        try {
            // Use the same simplified period ID format: T001-P001
            Enrollment enrollment = enrollmentService.enrollStudent(periodInfo.getSchoolId(), studentId, simplifiedPeriodId);
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
        if (code == null || code.length() < 11) { // Minimum: 4 + 4 + 3
            return null;
        }

        try {
            // Extract components (assuming 4-digit school ID and 4-char period ID)
            String schoolId = code.substring(0, 4);
            String periodId = code.substring(4, 8);
            String providedChecksum = code.substring(8);

            // Validate format
            if (!isValidSchoolId(schoolId) || !isValidPeriodId(periodId)) {
                return null;
            }

            // Validate checksum
            String expectedChecksum = generateChecksum(schoolId + periodId);
            if (!expectedChecksum.equals(providedChecksum)) {
                return null; // Invalid checksum
            }

            return new PeriodInfo(schoolId, periodId);
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
     * Validates school ID format (4 digits).
     */
    private boolean isValidSchoolId(String schoolId) {
        return schoolId != null && Pattern.matches("\\d{4}", schoolId);
    }

    /**
     * Validates period ID format (P followed by 3 digits).
     */
    private boolean isValidPeriodId(String periodId) {
        return periodId != null && Pattern.matches("P\\d{3}", periodId);
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
        private final String schoolId;
        private final String periodId;

        public PeriodInfo(String schoolId, String periodId) {
            this.schoolId = schoolId;
            this.periodId = periodId;
        }

        public String getSchoolId() { return schoolId; }
        public String getPeriodId() { return periodId; }
    }
}
