package com.openbake.settlement.presentation;

public record MonthlySettlementBatchResponse(
        Long jobExecutionId,
        String jobName,
        String status
) {
}