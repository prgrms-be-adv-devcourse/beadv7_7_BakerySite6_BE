package com.openbake.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    INVALID_STATE(HttpStatus.CONFLICT, "C002", "처리할 수 없는 상태입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "대상을 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C500", "서버 오류가 발생했습니다."),

    // Member Domain
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "ME001", "이미 존재하는 리소스입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "ME002", "유효하지 않은 인증 토큰입니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "ME003", "이메일 또는 비밀번호가 일치하지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ME004", "권한이 없습니다.."),

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

    //cart - CA
    CART_ALREADY_EXISTS(HttpStatus.CONFLICT, "CA001", "이미 장바구니에 담긴 상품이 있습니다."),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CA002", "장바구니가 없습니다."),
    CART_EXPIRED(HttpStatus.CONFLICT, "CA003", "장바구니가 만료되었습니다. 다시 담아주세요."),
    CART_INVALID_PICKUP_DATE(HttpStatus.BAD_REQUEST, "CA004", "선택할 수 없는 픽업 날짜입니다."),
    CART_PICKUP_DATE_UNAVAILABLE(HttpStatus.CONFLICT, "CA005", "이미 지난 픽업 날짜입니다."),

    // Settlement - ST
    SETTLEMENT_BATCH_ALREADY_COMPLETED(HttpStatus.CONFLICT,"S001","동일한 정산 기간의 배치가 이미 완료됐습니다."),
    SETTLEMENT_BATCH_ALREADY_RUNNING(HttpStatus.CONFLICT,"S002", "동일한 정산 기간의 배치가 이미 실행 중입니다."),
    SETTLEMENT_BATCH_RESTART_FAILED(HttpStatus.CONFLICT, "S003", "정산 배치를 재시작할 수 없습니다."),
    INVALID_SETTLEMENT_BATCH_PARAMETERS(HttpStatus.BAD_REQUEST, "S004", "정산 배치 파라미터가 올바르지 않습니다."),
    SETTLEMENT_BATCH_EXECUTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S500", "월 정산 배치 실행 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
