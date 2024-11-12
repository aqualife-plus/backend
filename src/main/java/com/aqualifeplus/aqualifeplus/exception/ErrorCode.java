package com.aqualifeplus.aqualifeplus.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_CREDENTIALS("잘못된 인증정보입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("존재하지 않는 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),

    USER_ALREADY_EXISTS("이미 가입한 회원입니다.", HttpStatus.CONFLICT),
    NOT_MATCH_PASSWORD_OR_EMAIL("이메일 or 비밀번호가 맞지 않습니다.", HttpStatus.UNAUTHORIZED),
    NOT_FOUND_MEMBER("존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND),;

    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}