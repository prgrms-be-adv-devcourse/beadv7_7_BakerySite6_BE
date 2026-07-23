package com.openbake.settlement.domain;

/**
 * 정산 대상의 처리 상태입니다.
 */
public enum SettlementTargetStatus {

    /**
     * 구매가 확정되어 정산을 기다리는 상태입니다.
     */
    PENDING,

    /**
     * 특정 정산 건에 포함된 상태입니다.
     */
    ASSIGNED,

    /**
     * 취소나 환불 등의 사유로 정산 대상에서 제외된 상태입니다.
     */
    EXCLUDED
}