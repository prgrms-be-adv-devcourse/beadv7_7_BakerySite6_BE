package com.openbake.settlement.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record SellerSettlementSummary(
        Long settlementId,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal grossSalesAmount,
        BigDecimal commissionAmount,
        BigDecimal adjustmentAmount,
        BigDecimal payoutAmount,
        Integer targetCount,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime completedAt
) {
}