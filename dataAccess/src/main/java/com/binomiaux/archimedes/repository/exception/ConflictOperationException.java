package com.binomiaux.archimedes.repository.exception;

public class ConflictOperationException extends RuntimeException {
    private final String reasonCode;

    public ConflictOperationException(String message, Throwable cause, String reasonCode) {
        super(message, cause);
        this.reasonCode = reasonCode;
    }

    public String getReasonCode() {
        return reasonCode;
    }
}