package com.binomiaux.archimedes.service.exception;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException(String message, Throwable cause) {
        super(message, cause);
    }
}