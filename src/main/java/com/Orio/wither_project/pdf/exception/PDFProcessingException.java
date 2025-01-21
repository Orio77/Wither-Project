package com.Orio.wither_project.pdf.exception;

public class PDFProcessingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PDFProcessingException(String message) {
        super(message);
    }

    public PDFProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PDFProcessingException(Throwable cause) {
        super(cause);
    }
}
