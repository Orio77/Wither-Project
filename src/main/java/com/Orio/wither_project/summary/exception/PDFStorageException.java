package com.Orio.wither_project.summary.exception;

/**
 * Exception thrown when storage operations fail
 */
public class PDFStorageException extends PDFProcessingException {
    public PDFStorageException(String message) {
        super(message);
    }

    public PDFStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
