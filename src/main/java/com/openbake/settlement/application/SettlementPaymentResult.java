package com.openbake.settlement.application;

import com.openbake.settlement.domain.Settlement;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SettlementPaymentResult(
        Long settlementId,
        Long sellerId,
        BigDecimal payoutAmount,
        String status,
        OffsetDateTime updatedAt,
        OffsetDateTime completedAt
) {

    public static SettlementPaymentResult from(
            Settlement settlement
    ) {
        return new SettlementPaymentResult(
                settlement.getId(),
                settlement.getSellerId(),
                settlement.getPayoutAmount(),
                settlement.getStatus().name(),
                settlement.getUpdatedAt(),
                settlement.getCompletedAt()
        );
    }
}