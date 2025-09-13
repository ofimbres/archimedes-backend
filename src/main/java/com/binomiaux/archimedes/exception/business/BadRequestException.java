package com.binomiaux.archimedes.exception.business;

/**
 * Exception that maps to HTTP 400 Bad Request
 */
public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
