package com.unicolombo.bienestar.exceptions;

public class BusinessException extends RuntimeException {
    private Object data;


    public BusinessException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public Object getData() {
        return data;
    }
    public BusinessException(String message) {
        super(message);
    }
}
