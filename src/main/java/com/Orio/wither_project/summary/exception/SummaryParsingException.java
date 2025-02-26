package com.Orio.wither_project.summary.exception;

/**
 * Exception thrown when parsing the summary from the model response fails
 */
public class SummaryParsingException extends RuntimeException {
    public SummaryParsingException(String message) {
        super(message);
    }

    public SummaryParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
