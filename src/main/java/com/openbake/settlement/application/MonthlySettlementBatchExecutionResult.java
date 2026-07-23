package com.openbake.settlement.application;

import java.time.LocalDateTime;

public record MonthlySettlementBatchExecutionResult(
        Long jobExecutionId,
        Long jobInstanceId,
        String jobName,
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String exitCode,
        String exitDescription,
        String periodStart,
        String periodEnd
) {
}