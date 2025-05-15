package com.unicolombo.bienestar.exceptions;

public class DataIntegrityException extends RuntimeException {
    public DataIntegrityException(String message, Throwable cause) {
        super(message, cause);
    }
}