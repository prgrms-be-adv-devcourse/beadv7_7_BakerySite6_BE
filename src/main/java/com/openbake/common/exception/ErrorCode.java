package com.openbake.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    INVALID_STATE(HttpStatus.CONFLICT, "C002", "처리할 수 없는 상태입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "대상을 찾을 수 없습니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "C004", "이미 존재하는 리소스입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "C005", "유효하지 않은 인증 토큰입니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "C006", "이메일 또는 비밀번호가 일치하지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C007", "권한이 없습니다.."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C500", "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
