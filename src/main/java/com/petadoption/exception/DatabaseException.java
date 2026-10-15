package com.petadoption.exception;

/** Wraps any JDBC / SQL problem so upper layers never deal with SQLException. */
public class DatabaseException extends AppException {
    private static final long serialVersionUID = 1L;

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
