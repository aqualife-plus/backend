package com.aqualifeplus.aqualifeplus.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_CREDENTIALS("잘못된 인증정보입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("존재하지 않는 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),

    USER_ALREADY_EXISTS("이미 가입한 회원입니다.", HttpStatus.CONFLICT),
    NOT_MATCH_PASSWORD_OR_EMAIL("이메일 or 비밀번호가 맞지 않습니다.", HttpStatus.UNAUTHORIZED),
    NOT_FOUND_MEMBER("존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND),
    NULL_AND_NOT_FORMAT_EMAIL("값이 없거나 이메일 형식이 아닙니다", HttpStatus.BAD_REQUEST),

    NOT_MATCH_UUID_FORMAT("UUID 형식이 아닙니다.", HttpStatus.BAD_REQUEST),

    FAIL_UPDATE_NAME("어항의 이름을 수정하지 못했습니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND_NEW_FISHBOWL_ID_USE_THIS_USER_ID("해당 유저가 만든 어항이 존재하지 않습니다.", HttpStatus.NOT_FOUND),

    NOT_MATCH_NUMBER_FORMAT("숫자 형식이 아닙니다.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
