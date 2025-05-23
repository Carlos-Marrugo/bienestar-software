package com.unicolombo.bienestar.exceptions;

import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Map;

public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final Object data;

    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.data = null;
    }

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.data = null;
    }

    public BusinessException(String message, List<Long> conflictIds) {
        super(message);
        this.status = HttpStatus.CONFLICT;
        this.data = conflictIds;
    }

    public BusinessException(String message, Map<String, Object> conflictData) {
        super(message);
        this.status = HttpStatus.CONFLICT;
        this.data = conflictData;
    }

    public BusinessException(String message, List<Map<String, Object>> conflictData, HttpStatus status) {
        super(message);
        this.status = status;
        this.data = conflictData;
    }



    public HttpStatus getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }
}