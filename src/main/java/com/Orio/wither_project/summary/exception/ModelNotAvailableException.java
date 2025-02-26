package com.Orio.wither_project.summary.exception;

/**
 * Exception thrown when the AI model is not available or returns an error
 */
public class ModelNotAvailableException extends RuntimeException {
    public ModelNotAvailableException(String message) {
        super(message);
    }

    public ModelNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
