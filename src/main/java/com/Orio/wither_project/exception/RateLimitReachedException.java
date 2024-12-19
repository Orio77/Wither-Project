package com.Orio.wither_project.exception;

public class RateLimitReachedException extends Exception {

    public RateLimitReachedException(String message) {
        super(message);
    }

    public RateLimitReachedException(String message, Throwable cause) {
        super(message, cause);
    }
}
