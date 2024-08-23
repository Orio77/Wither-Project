package com.Orio.web_scraping_tool.exception;

public class DataSourceUnavailableException extends Exception {

    public DataSourceUnavailableException(String message) {
        super(message);
    }

    public DataSourceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
