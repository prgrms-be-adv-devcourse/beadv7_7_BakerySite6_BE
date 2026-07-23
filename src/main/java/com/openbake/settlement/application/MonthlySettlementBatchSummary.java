package com.openbake.settlement.application;

import java.time.LocalDateTime;

public record MonthlySettlementBatchSummary(
        Long jobExecutionId,
        Long jobInstanceId,
        String jobName,
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String exitCode,
        String periodStart,
        String periodEnd
) {
}