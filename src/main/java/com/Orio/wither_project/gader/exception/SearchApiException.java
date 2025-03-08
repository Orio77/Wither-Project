package com.Orio.wither_project.gader.exception;

public class SearchApiException extends SearchException {

    private final int statusCode;

    public SearchApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public SearchApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}