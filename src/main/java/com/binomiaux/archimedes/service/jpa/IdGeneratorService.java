package com.binomiaux.archimedes.service.jpa;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

/**
 * JPA-based service for generating unique IDs for various entities.
 * Much simpler than DynamoDB counters!
 */
@Service("jpaIdGeneratorService")
public class IdGeneratorService {

    /**
     * Generate a unique student ID based on school ID and current date
     */
    public String generateStudentId(String schoolId) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%04d", ThreadLocalRandom.current().nextInt(1000, 10000));
        return schoolId + "-STU-" + datePart + "-" + randomPart;
    }

    /**
     * Generate a unique teacher ID based on school ID and current date
     */
    public String generateTeacherId(String schoolId) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%04d", ThreadLocalRandom.current().nextInt(1000, 10000));
        return schoolId + "-TCH-" + datePart + "-" + randomPart;
    }

    /**
     * Generate a unique period ID based on school ID and current date
     */
    public String generatePeriodId(String schoolId) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%04d", ThreadLocalRandom.current().nextInt(1000, 10000));
        return schoolId + "-PER-" + datePart + "-" + randomPart;
    }

    /**
     * Generate a unique school ID
     */
    public String generateSchoolId() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 1000000));
        return "SCH-" + datePart + "-" + randomPart;
    }
}