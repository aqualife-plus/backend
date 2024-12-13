package com.aqualifeplus.aqualifeplus.common.exception;

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
