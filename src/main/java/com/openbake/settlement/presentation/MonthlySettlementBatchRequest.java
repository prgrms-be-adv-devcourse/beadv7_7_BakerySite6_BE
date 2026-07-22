package com.openbake.settlement.presentation;

import java.time.LocalDate;

public record MonthlySettlementBatchRequest(
        LocalDate periodStart,
        LocalDate periodEnd
) {
}