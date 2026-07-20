package com.openbake.settlement.presentation;

import com.openbake.settlement.application.SettlementEventResult;

import java.util.UUID;

/**
 * 구매확정 이벤트 처리 API의 응답 DTO입니다.
 */
public record SettlementEventResponse(
        UUID eventId,
        Long settlementTargetId,
        boolean duplicate
) {

    /**
     * 애플리케이션 처리 결과를 HTTP 응답 DTO로 변환합니다.
     */
    public static SettlementEventResponse from(
            SettlementEventResult result
    ) {
        return new SettlementEventResponse(
                result.eventId(),
                result.settlementTargetId(),
                result.duplicate()
        );
    }
}