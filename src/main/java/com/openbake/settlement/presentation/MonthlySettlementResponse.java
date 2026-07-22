package com.openbake.settlement.presentation;

import com.openbake.settlement.application.MonthlySettlementResult;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlySettlementResponse(
        LocalDate periodStart,
        LocalDate periodEnd,
        int settlementCount,
        int targetCount,
        BigDecimal totalPayoutAmount
) {

    public static MonthlySettlementResponse from(
            MonthlySettlementResult result
    ) {
        return new MonthlySettlementResponse(
                result.periodStart(),
                result.periodEnd(),
                result.settlementCount(),
                result.targetCount(),
                result.totalPayoutAmount()
        );
    }
}