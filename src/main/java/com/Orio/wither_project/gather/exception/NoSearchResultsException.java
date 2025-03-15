package com.Orio.wither_project.gather.exception;

public class NoSearchResultsException extends SearchException {

    public NoSearchResultsException() {
        super("No search results found");
    }

    public NoSearchResultsException(String message) {
        super(message);
    }
}