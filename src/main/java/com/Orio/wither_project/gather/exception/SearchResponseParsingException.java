package com.Orio.wither_project.gather.exception;

public class SearchResponseParsingException extends SearchException {

    public SearchResponseParsingException(String message) {
        super(message);
    }

    public SearchResponseParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}