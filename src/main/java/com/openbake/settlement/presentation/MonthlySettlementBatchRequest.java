package com.openbake.settlement.presentation;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MonthlySettlementBatchRequest(

        @NotNull(message = "periodStart는 필수입니다.")
        LocalDate periodStart,

        @NotNull(message = "periodEnd는 필수입니다.")
        LocalDate periodEnd
) {
}