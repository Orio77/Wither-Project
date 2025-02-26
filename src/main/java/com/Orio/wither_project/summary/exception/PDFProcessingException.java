package com.Orio.wither_project.summary.exception;

/**
 * Base exception for PDF processing errors
 */
public class PDFProcessingException extends RuntimeException {
    public PDFProcessingException(String message) {
        super(message);
    }

    public PDFProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
