package com.aqualifeplus.aqualifeplus.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

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
