package com.gurula.stockMate.exception;

import com.gurula.stockMate.config.ConfigProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final ConfigProperties configProperties;

    public GlobalExceptionHandler(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<String> handleInvalidTokenException(InvalidTokenException ex) {
        String errorMessage = "Invalid token. Please log in again.";
        return new ResponseEntity<>(errorMessage, HttpStatus.UNAUTHORIZED); // 401 Unauthorized
    }
}
