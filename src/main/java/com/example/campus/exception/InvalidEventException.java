package com.example.campus.exception;

public class InvalidEventException extends RuntimeException {

    public InvalidEventException(String message) {
        super(message);
    }
}