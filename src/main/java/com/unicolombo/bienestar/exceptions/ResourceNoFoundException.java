package com.unicolombo.bienestar.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ResourceNoFoundException extends BusinessException{

    private HttpStatus status = HttpStatus.NOT_FOUND;

    public ResourceNoFoundException(String message) {
        super(message);
    }
}
