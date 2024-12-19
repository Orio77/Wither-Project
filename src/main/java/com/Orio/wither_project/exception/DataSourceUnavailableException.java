package com.Orio.wither_project.exception;

public class DataSourceUnavailableException extends Exception {

    public DataSourceUnavailableException(String message) {
        super(message);
    }

    public DataSourceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
