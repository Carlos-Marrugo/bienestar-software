package com.unicolombo.bienestar.utils;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseWrapper {

    public static Map<String, Object> success(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    public static Map<String, Object> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    public static Map<String, Object> error(String message, BindingResult result) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());

        Map<String, Object> validationDetails = new HashMap<>();
        List<Map<String, String>> fieldErrors = new ArrayList<>();

        for (FieldError error : result.getFieldErrors()) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("field", error.getField());
            errorMap.put("message", error.getDefaultMessage());
            fieldErrors.add(errorMap);
        }

        validationDetails.put("count", result.getErrorCount());
        validationDetails.put("errors", fieldErrors);
        response.put("validation", validationDetails);

        return response;
    }

    public static Map<String, Object> validationError(BindingResult result) {
        String errorMessage = !result.getFieldErrors().isEmpty()
                ? result.getFieldErrors().get(0).getDefaultMessage()
                : "Error de validación";

        return error(errorMessage, result);
    }
}