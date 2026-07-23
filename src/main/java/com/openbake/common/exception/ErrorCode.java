package com.openbake.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 공용
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    INVALID_STATE(HttpStatus.CONFLICT, "C002", "처리할 수 없는 상태입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "대상을 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C500", "서버 오류가 발생했습니다."),

    // 결제 — 충전
    INVALID_CHARGE_AMOUNT(HttpStatus.BAD_REQUEST, "P000", "충전 금액이 올바르지 않습니다."),
    CHARGE_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "존재하지 않는 충전 요청입니다."),
    CHARGE_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "P003", "본인의 충전 요청이 아닙니다."),
    CHARGE_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "P004", "충전 금액이 일치하지 않습니다."),
    CHARGE_NOT_APPROVABLE(HttpStatus.CONFLICT, "P005", "승인할 수 없는 충전 상태입니다."),
    CHARGE_EXPIRED(HttpStatus.CONFLICT, "P006", "만료된 충전 요청입니다."),
    PG_APPROVE_FAILED(HttpStatus.BAD_GATEWAY, "P007", "결제 승인에 실패했습니다."),
    PG_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "P008", "결제 결과를 확인 중입니다. 잠시 후 내역을 확인해주세요."),

    // 결제 — 예치금
    INVALID_TRANSACTION_TYPE(HttpStatus.BAD_REQUEST, "P100", "유효하지 않은 거래 유형입니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "P101", "예치금 잔액이 부족합니다."),

    // 결제 — 주문 결제
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P201", "존재하지 않는 결제입니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.CONFLICT, "P202", "처리할 수 없는 결제 상태입니다."),
    DEPOSIT_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "P203", "예치금 계좌를 찾을 수 없습니다."),
    PLATFORM_ACCOUNT_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "P204", "PLATFORM 계정이 존재하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
