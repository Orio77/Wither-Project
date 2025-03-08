package com.Orio.wither_project.gader.exception;

public class SearchResponseParsingException extends SearchException {

    public SearchResponseParsingException(String message) {
        super(message);
    }

    public SearchResponseParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}