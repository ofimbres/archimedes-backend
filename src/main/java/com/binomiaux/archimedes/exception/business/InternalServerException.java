package com.binomiaux.archimedes.exception.business;

/**
 * Exception that maps to HTTP 500 Internal Server Error
 */
public class InternalServerException extends RuntimeException {
    
    public InternalServerException(String message) {
        super(message);
    }
    
    public InternalServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
