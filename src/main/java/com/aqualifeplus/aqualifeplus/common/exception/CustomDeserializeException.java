package com.aqualifeplus.aqualifeplus.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomDeserializeException extends RuntimeException{
    private final String errorKey;
    private final String message;
}
