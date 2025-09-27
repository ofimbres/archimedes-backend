package com.binomiaux.archimedes.service.jpa;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.binomiaux.archimedes.entity.UserIdCounter;
import com.binomiaux.archimedes.repository.jpa.UserIdCounterRepository;

/**
 * Service for generating sequential user IDs per school.
 * Generates clean IDs like: STU-001, STU-002, TCH-001, etc.
 */
@Service
@Transactional
public class SequentialIdGeneratorService {

    private static final String STUDENT_TYPE = "STUDENT";
    private static final String TEACHER_TYPE = "TEACHER";

    private final UserIdCounterRepository counterRepository;

    public SequentialIdGeneratorService(UserIdCounterRepository counterRepository) {
        this.counterRepository = counterRepository;
    }

    /**
     * Generate next sequential student ID for a school.
     * Format: STU-001, STU-002, STU-003, etc.
     */
    public String generateStudentId(Long schoolId) {
        Integer sequence = getNextSequence(schoolId, STUDENT_TYPE);
        return String.format("STU-%03d", sequence);
    }

    /**
     * Generate next sequential teacher ID for a school.
     * Format: TCH-001, TCH-002, TCH-003, etc.
     */
    public String generateTeacherId(Long schoolId) {
        Integer sequence = getNextSequence(schoolId, TEACHER_TYPE);
        return String.format("TCH-%03d", sequence);
    }

    /**
     * Get next sequence number for a school and user type.
     * Thread-safe with pessimistic locking.
     */
    private Integer getNextSequence(Long schoolId, String userType) {
        // Try to find existing counter with lock
        UserIdCounter counter = counterRepository
            .findBySchoolIdAndUserTypeWithLock(schoolId, userType)
            .orElse(null);

        if (counter == null) {
            // Create new counter if it doesn't exist
            counter = new UserIdCounter(schoolId, userType);
        }

        // Get current sequence and increment
        Integer sequence = counter.getAndIncrement();
        
        // Save the updated counter
        counterRepository.save(counter);
        
        return sequence;
    }
}