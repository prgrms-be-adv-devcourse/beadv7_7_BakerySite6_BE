package com.openbake.settlement.presentation;

import com.openbake.settlement.application.ReceivePurchaseConfirmedCommand;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 구매확정 이벤트 수신 API의 요청 DTO입니다.
 *
 * HTTP 요청으로 받은 데이터를
 * 애플리케이션 계층의 Command로 변환합니다.
 */
public record PurchaseConfirmedRequest(
        UUID eventId,
        Long orderId,
        Long orderItemId,
        Long sellerId,
        String productName,
        Integer quantity,
        BigDecimal grossAmount,
        OffsetDateTime confirmedAt
) {

    /**
     * HTTP 요청 DTO를 애플리케이션 Command로 변환합니다.
     */
    public ReceivePurchaseConfirmedCommand toCommand() {
        return new ReceivePurchaseConfirmedCommand(
                eventId,
                orderId,
                orderItemId,
                sellerId,
                productName,
                quantity,
                grossAmount,
                confirmedAt
        );
    }
}