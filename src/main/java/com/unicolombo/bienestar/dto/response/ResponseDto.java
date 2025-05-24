package com.unicolombo.bienestar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class ResponseDto<T> {
    private int status;
    private String message;
    private T data;

    public ResponseDto(HttpStatus status, String message, T data) {
        this.status = status.value();
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseDto<T> success(T data) {
        return new ResponseDto<>(HttpStatus.OK, "Operación exitosa", data);
    }

    public static <T> ResponseDto<T> success(String message, T data) {
        return new ResponseDto<>(HttpStatus.OK, message, data);
    }

    public static <T> ResponseDto<T> error(HttpStatus status, String message) {
        return new ResponseDto<>(status, message, null);
    }

}

