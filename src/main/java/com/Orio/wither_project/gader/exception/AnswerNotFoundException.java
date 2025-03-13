package com.Orio.wither_project.gader.exception;

/**
 * Exception thrown when an answer cannot be found in the content based on
 * provided markers.
 */
public class AnswerNotFoundException extends RuntimeException {

    public AnswerNotFoundException(String message) {
        super(message);
    }

    public AnswerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
