package com.example.campus.exception;

public class EventDeletionConflictException extends RuntimeException{
    public EventDeletionConflictException(String message) {
        super(message);
    }
}
