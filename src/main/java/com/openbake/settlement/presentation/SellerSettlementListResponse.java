package com.openbake.settlement.presentation;

import com.openbake.settlement.application.SellerSettlementSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record SellerSettlementListResponse(
        List<Item> settlements
) {

    public static SellerSettlementListResponse from(
            List<SellerSettlementSummary> summaries
    ) {
        List<Item> items = summaries.stream()
                .map(Item::from)
                .toList();

        return new SellerSettlementListResponse(items);
    }

    public record Item(
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

        private static Item from(
                SellerSettlementSummary summary
        ) {
            return new Item(
                    summary.settlementId(),
                    summary.periodStart(),
                    summary.periodEnd(),
                    summary.grossSalesAmount(),
                    summary.commissionAmount(),
                    summary.adjustmentAmount(),
                    summary.payoutAmount(),
                    summary.targetCount(),
                    summary.status(),
                    summary.createdAt(),
                    summary.completedAt()
            );
        }
    }
}