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
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C500", "서버 오류가 발생했습니다."),
    /** 정산 배치 오류 */
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
