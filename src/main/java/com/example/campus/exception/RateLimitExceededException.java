package com.example.campus.exception;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException() {
        super("Too many reservation attempts. Try again later.");
    }
}
