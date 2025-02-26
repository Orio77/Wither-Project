package com.Orio.wither_project.summary.exception;

/**
 * Exception thrown when content extraction fails
 */
public class PDFContentExtractionException extends PDFProcessingException {
    public PDFContentExtractionException(String message) {
        super(message);
    }

    public PDFContentExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
