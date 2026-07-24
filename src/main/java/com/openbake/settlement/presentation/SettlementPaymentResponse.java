package com.openbake.settlement.presentation;

import com.openbake.settlement.application.SettlementPaymentResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SettlementPaymentResponse(
        Long settlementId,
        Long sellerId,
        BigDecimal payoutAmount,
        String status,
        OffsetDateTime updatedAt,
        OffsetDateTime completedAt
) {

    public static SettlementPaymentResponse from(
            SettlementPaymentResult result
    ) {
        return new SettlementPaymentResponse(
                result.settlementId(),
                result.sellerId(),
                result.payoutAmount(),
                result.status(),
                result.updatedAt(),
                result.completedAt()
        );
    }
}