package com.Orio.wither_project.query.exception;

public class EmptyVectorDatabaseException extends RuntimeException {

    public EmptyVectorDatabaseException(String message) {
        super(message);
    }

    public EmptyVectorDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyVectorDatabaseException(Throwable cause) {
        super(cause);
    }
}
