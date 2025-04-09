package com.Orio.wither_project.process.qa.exception;

public class InformationPieceNotFoundException extends RuntimeException {

    public InformationPieceNotFoundException(String message) {
        super(message);
    }

    public InformationPieceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
