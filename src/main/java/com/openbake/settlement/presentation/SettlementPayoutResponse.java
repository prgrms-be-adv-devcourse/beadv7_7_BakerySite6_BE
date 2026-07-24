package com.openbake.settlement.presentation;

import com.openbake.settlement.application.SettlementPayoutResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SettlementPayoutResponse(
        Long payoutId,
        Long settlementId,
        Long sellerId,
        BigDecimal payoutAmount,
        String idempotencyKey,
        String status,
        String externalTransactionId,
        String failureReason,
        OffsetDateTime requestedAt,
        OffsetDateTime completedAt,
        OffsetDateTime failedAt
) {

    public static SettlementPayoutResponse from(
            SettlementPayoutResult result
    ) {
        return new SettlementPayoutResponse(
                result.payoutId(),
                result.settlementId(),
                result.sellerId(),
                result.payoutAmount(),
                result.idempotencyKey(),
                result.status(),
                result.externalTransactionId(),
                result.failureReason(),
                result.requestedAt(),
                result.completedAt(),
                result.failedAt()
        );
    }
}