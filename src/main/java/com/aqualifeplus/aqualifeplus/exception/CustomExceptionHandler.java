package com.aqualifeplus.aqualifeplus.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(e.getStatus())
                .errorCode(e.getErrorCode())
                .message(e.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, e.getStatus());
    }
}

