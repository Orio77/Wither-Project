package com.Orio.wither_project.summary.exception;

/**
 * Exception thrown when summary generation fails
 */
public class PDFSummaryGenerationException extends PDFProcessingException {
    public PDFSummaryGenerationException(String message) {
        super(message);
    }

    public PDFSummaryGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
