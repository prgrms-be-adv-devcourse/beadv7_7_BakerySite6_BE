package com.openbake.settlement.presentation;

import com.openbake.settlement.application
        .MonthlySettlementBatchExecutionResult;

import java.time.LocalDateTime;

public record MonthlySettlementBatchExecutionResponse(
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

    public static MonthlySettlementBatchExecutionResponse from(
            MonthlySettlementBatchExecutionResult result
    ) {
        return new MonthlySettlementBatchExecutionResponse(
                result.jobExecutionId(),
                result.jobInstanceId(),
                result.jobName(),
                result.status(),
                result.startTime(),
                result.endTime(),
                result.exitCode(),
                result.exitDescription(),
                result.periodStart(),
                result.periodEnd()
        );
    }
}