package com.binomiaux.archimedes.exception.business;

/**
 * Exception thrown when attempting to create an entity with a duplicate email address.
 */
public class DuplicateEmailException extends RuntimeException {
    
    public DuplicateEmailException(String message) {
        super(message);
    }
    
    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
