package com.example.campus.exception;

public class WaitlistEntryNotFoundException extends RuntimeException {
    public WaitlistEntryNotFoundException(String message) {
        super(message);
    }
}
