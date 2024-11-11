package com.aqualifeplus.aqualifeplus.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
public class ErrorResponse {

    private HttpStatus status;
    private ErrorCode errorCode;
    private String message;
}
