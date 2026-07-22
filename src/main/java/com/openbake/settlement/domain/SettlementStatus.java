package com.openbake.settlement.domain;

/**
 * 판매자별 월 정산의 처리 상태입니다.
 */
public enum SettlementStatus {

    /**
     * 정산 금액 집계가 완료되어 지급 가능한 상태입니다.
     */
    READY,

    /**
     * 계좌 미검증, 관리자 검토 등의 이유로 지급이 보류된 상태입니다.
     */
    ON_HOLD,

    /**
     * 판매자 계좌로 지급을 진행 중인 상태입니다.
     */
    PAYING,

    /**
     * 지급에 실패하여 재처리가 필요한 상태입니다.
     */
    FAILED,

    /**
     * 판매자 지급까지 완료된 상태입니다.
     */
    COMPLETED
}