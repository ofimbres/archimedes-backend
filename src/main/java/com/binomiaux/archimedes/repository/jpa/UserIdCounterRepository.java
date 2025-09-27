package com.binomiaux.archimedes.repository.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.binomiaux.archimedes.entity.UserIdCounter;
import com.binomiaux.archimedes.entity.UserIdCounterKey;

import jakarta.persistence.LockModeType;

/**
 * Repository for UserIdCounter entity.
 * Handles sequential ID generation per school and user type.
 */
@Repository
public interface UserIdCounterRepository extends JpaRepository<UserIdCounter, UserIdCounterKey> {

    /**
     * Find counter by school and user type with pessimistic lock.
     * This ensures thread-safe sequence generation.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserIdCounter u WHERE u.schoolId = ?1 AND u.userType = ?2")
    Optional<UserIdCounter> findBySchoolIdAndUserTypeWithLock(Long schoolId, String userType);
}