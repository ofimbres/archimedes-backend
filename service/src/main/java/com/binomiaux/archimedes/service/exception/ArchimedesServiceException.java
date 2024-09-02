package com.binomiaux.archimedes.service.exception;

public class ArchimedesServiceException extends RuntimeException {

    public ArchimedesServiceException(String message) {
        super(message);
    }

    public ArchimedesServiceException(String message, Exception exception) {
        super(message, exception);
    }
}