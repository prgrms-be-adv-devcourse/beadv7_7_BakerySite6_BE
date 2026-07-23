package com.openbake.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    INVALID_STATE(HttpStatus.CONFLICT, "C002", "처리할 수 없는 상태입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "대상을 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C500", "서버 오류가 발생했습니다."),

    // Drop Domain
    DROP_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "존재하지 않는 드롭입니다."),
    INVALID_DROP_TIME(HttpStatus.BAD_REQUEST, "D002", "드롭 시작 시간 또는 마감 시간이 유효하지 않습니다."),
    INVALID_PICKUP_DATE(HttpStatus.BAD_REQUEST, "D003", "픽업 가능 날짜는 드롭 마감일 이후여야 합니다."),
    DUPLICATE_DROP_DATE(HttpStatus.CONFLICT, "D004", "해당 날짜에는 이미 등록된 드롭이 존재합니다."),
    INVALID_QUANTITY_LIMIT(HttpStatus.BAD_REQUEST, "D005", "1인당 제한 수량은 총 수량보다 클 수 없습니다."),

    // Drop Entry Domain
    ALREADY_ENTERED(HttpStatus.CONFLICT, "E001", "이미 응모 완료된 드롭입니다."),
    DROP_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "E002", "준비된 재고가 모두 소진되었습니다."),
    DROP_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "E003", "현재 응모 가능한 드롭 기간이 아닙니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "C004", "이미 존재하는 리소스입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "C005", "유효하지 않은 인증 토큰입니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "C006", "이메일 또는 비밀번호가 일치하지 않습니다."),

    //cart - CA
    CART_ALREADY_EXISTS(HttpStatus.CONFLICT, "CA001", "이미 장바구니에 담긴 상품이 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
