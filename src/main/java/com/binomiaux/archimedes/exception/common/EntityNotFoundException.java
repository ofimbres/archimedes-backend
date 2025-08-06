package com.binomiaux.archimedes.exception.common;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}