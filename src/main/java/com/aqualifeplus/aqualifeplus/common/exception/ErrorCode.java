package com.aqualifeplus.aqualifeplus.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    //token관련 error
    INVALID_CREDENTIALS("잘못된 인증정보입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("존재하지 않는 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),

    //data관련 error
    USER_ALREADY_EXISTS("이미 가입한 회원입니다.", HttpStatus.CONFLICT),
    NOT_MATCH_PASSWORD_OR_EMAIL("이메일 or 비밀번호가 맞지 않습니다.", HttpStatus.UNAUTHORIZED),
    NOT_FOUND_MEMBER("존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND),
    NULL_AND_NOT_FORMAT_EMAIL("값이 없거나 이메일 형식이 아닙니다", HttpStatus.BAD_REQUEST),
    NOT_MATCH_NUMBER_FORMAT("숫자 형식이 아닙니다.", HttpStatus.BAD_REQUEST),
    NOT_MATCH_UUID_FORMAT("UUID 형식이 아닙니다.", HttpStatus.BAD_REQUEST),
    FAIL_UPDATE_NAME("어항의 이름을 수정하지 못했습니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID("해당 유저가 만든 어항이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    NOT_MATCH_UPDATE_COLUMN("원하는 컬럼값이 들어오지않았습니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND_LIGHT_RESERVE("해당 데이터로 Light 예약이 없습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_CO2_RESERVE("해당 데이터로 Co2 예약이 없습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_FILTER("해당 Filter가 없습니다.", HttpStatus.NOT_FOUND),

    DISCONNECTED_FIREBASE_SERVER("firebase와 연결이 끊겼습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    DISCONNECTED_REDIS("Redis와 연결이 끊겼습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    PERMISSION_DENIED_FIREBASE_SERVER("firebase와 연결이 허용되지 않습니다.", HttpStatus.BAD_GATEWAY),
    NETWORK_FIREBASE_ERROR("네트워크 에러로 firebase와 소통하지 못했습니다.", HttpStatus.BAD_GATEWAY),
    FIREBASE_ERROR("firebase 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    THREAD_INTERRUPTED("현재 스레드가 대기 중 인터럽트를 받았습니다.", HttpStatus.REQUEST_TIMEOUT),
    FAIL_FIREBASE_SAVE("firebase에 데이터를 보내지 못했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FAIL_UPDATE_NOW_CO2("실시간 co2에 데이터를 저장하지 못했습니다.", HttpStatus.BAD_REQUEST),
    FAIL_UPDATE_NOW_LIGHT("실시간 light에 데이터를 저장하지 못했습니다.", HttpStatus.BAD_REQUEST),
    FAIL_UPDATE_NOW_FILTER("실시간 filter에 데이터를 저장하지 못했습니다.", HttpStatus.BAD_REQUEST),
    NOT_MATCH_NOW_DATA_FORMAT("정해진 형식이 아닙니다.",HttpStatus.BAD_REQUEST),
    DUPLICATE_KEY_IN_REDIS("Redis에서 중복된 키로 인해 작업이 실패했습니다.", HttpStatus.BAD_REQUEST),
    DATA_INTEGRITY_VIOLATION_IN_REDIS("데이터 무결성 조건을 위반했습니다.", HttpStatus.BAD_REQUEST),
    DATA_ACCESS_ERROR_IN_REDIS("Redis 데이터 접근 중 문제가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    VAlUE_NOT_FOUND_IN_REDIS("해당 키에 데이터가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    DATA_INTEGRITY_VIOLATION_IN_JPA("JPA에서 데이터 무결성 위반.", HttpStatus.BAD_REQUEST),
    ENTITY_NOT_FOUND_IN_JPA("JPA에서 엔티티를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DATA_ACCESS_ERROR_IN_JPA("JPA에서 데이터베이스 접근 오류.", HttpStatus.INTERNAL_SERVER_ERROR),
    UNEXPECTED_ERROR_IN_JPA("JPA에서 예상치 못한 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_CORRECT_EXPIRED_KEY_IN_REDIS("의도하지 않은 Key입니다.", HttpStatus.BAD_REQUEST),

    NOT_FOUND_KEY_IN_REDIS("해당 키가 존재하지 않습니다.", HttpStatus.NOT_FOUND),

    RABBITMQ_BASIC_REJECT_ERROR("메세지 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
