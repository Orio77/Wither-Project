package com.Orio.web_scraping_tool.exception;

public class WebSearchException extends Exception {

    public WebSearchException(String message) {
        super(message);
    }

    public WebSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
