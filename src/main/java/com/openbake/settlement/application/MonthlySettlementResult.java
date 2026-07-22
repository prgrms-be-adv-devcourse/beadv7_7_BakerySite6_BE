package com.openbake.settlement.application;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 월 정산 실행 결과 DTO
 * 특정 기간의 월 정산 처리 결과입니다.
 */
public record MonthlySettlementResult(
        LocalDate periodStart,
        LocalDate periodEnd,
        int settlementCount,
        int targetCount,
        BigDecimal totalPayoutAmount
) {

    public static MonthlySettlementResult empty(
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        return new MonthlySettlementResult(
                periodStart,
                periodEnd,
                0,
                0,
                new BigDecimal("0.00")
        );
    }
}