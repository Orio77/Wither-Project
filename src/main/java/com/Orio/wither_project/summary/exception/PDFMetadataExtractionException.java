package com.Orio.wither_project.summary.exception;

/**
 * Exception thrown when metadata extraction fails
 */
public class PDFMetadataExtractionException extends PDFProcessingException {
    public PDFMetadataExtractionException(String message) {
        super(message);
    }

    public PDFMetadataExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
