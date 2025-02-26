package com.Orio.wither_project.summary.exception;

/**
 * Exception thrown when PDF conversion fails
 */
public class PDFConversionException extends PDFProcessingException {
    public PDFConversionException(String message) {
        super(message);
    }

    public PDFConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
