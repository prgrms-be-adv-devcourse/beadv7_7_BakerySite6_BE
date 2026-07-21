package com.openbake.settlement.application;

/**
 * 구매확정 이벤트 처리 결과입니다.
 * 정산 대상이 새로 생성되었는지,
 * 이미 처리된 중복 이벤트인지 나타냅니다.
 */
public record SettlementEventResult(
        String eventId,
        Long settlementTargetId,
        boolean duplicate
) {
    /**
     * 새로운 정산 대상이 정상적으로 생성된 결과입니다.
     */
    public static SettlementEventResult created(
            String eventId,
            Long settlementTargetId
    ) {
        return new SettlementEventResult(
                eventId,
                settlementTargetId,
                false
        );
    }

    /**
     * 이미 처리된 이벤트이거나 동일한 주문 상품이 존재하는 결과입니다.
     */
    public static SettlementEventResult duplicated(
            String eventId,
            Long settlementTargetId
    ) {
        return new SettlementEventResult(
                eventId,
                settlementTargetId,
                true
        );
    }
}