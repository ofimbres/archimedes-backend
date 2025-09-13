package com.binomiaux.archimedes.exception.business;

/**
 * Exception that maps to HTTP 409 Conflict
 */
public class ConflictException extends RuntimeException {
    
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
