package com.binomiaux.archimedes.exception.business;

/**
 * Exception thrown when a teacher would exceed their maximum allowed periods.
 */
public class MaxPeriodsExceededException extends RuntimeException {
    
    public MaxPeriodsExceededException(String message) {
        super(message);
    }
    
    public MaxPeriodsExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
