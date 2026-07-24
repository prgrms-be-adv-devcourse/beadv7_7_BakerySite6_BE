package com.openbake.settlement.application;

import com.openbake.settlement.domain.SettlementPayout;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SettlementPayoutResult(
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

    public static SettlementPayoutResult from(
            SettlementPayout payout
    ) {
        return new SettlementPayoutResult(
                payout.getId(),
                payout.getSettlementId(),
                payout.getSellerId(),
                payout.getPayoutAmount(),
                payout.getIdempotencyKey(),
                payout.getStatus().name(),
                payout.getExternalTransactionId(),
                payout.getFailureReason(),
                payout.getRequestedAt(),
                payout.getCompletedAt(),
                payout.getFailedAt()
        );
    }
}