package com.Orio.wither_project.summary.exception;

/**
 * Exception thrown when the AI model response is invalid
 */
public class InvalidResponseException extends RuntimeException {
    public InvalidResponseException(String message) {
        super(message);
    }

    public InvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
