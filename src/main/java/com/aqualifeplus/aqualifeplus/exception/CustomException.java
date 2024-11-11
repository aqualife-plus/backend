package com.aqualifeplus.aqualifeplus.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException  extends RuntimeException{
    private final HttpStatus status;
    private final ErrorCode errorCode;
    private final String message;

    public CustomException(ErrorCode errorCode) {
        this.status = errorCode.getStatus();
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }
}
