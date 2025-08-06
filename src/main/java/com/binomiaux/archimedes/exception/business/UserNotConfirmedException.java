package com.binomiaux.archimedes.exception.business;

public class UserNotConfirmedException extends RuntimeException {
    public UserNotConfirmedException(String message) {
        super(message);
    }
}