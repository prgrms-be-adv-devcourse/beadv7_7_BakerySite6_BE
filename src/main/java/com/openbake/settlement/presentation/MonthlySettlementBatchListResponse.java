package com.openbake.settlement.presentation;

import com.openbake.settlement.application.MonthlySettlementBatchListResult;
import com.openbake.settlement.application.MonthlySettlementBatchSummary;

import java.time.LocalDateTime;
import java.util.List;

public record MonthlySettlementBatchListResponse(
        List<Item> executions,
        int page,
        int size,
        boolean hasNext
) {

    public static MonthlySettlementBatchListResponse from(
            MonthlySettlementBatchListResult result
    ) {
        List<Item> items = result.executions()
                .stream()
                .map(Item::from)
                .toList();

        return new MonthlySettlementBatchListResponse(
                items,
                result.page(),
                result.size(),
                result.hasNext()
        );
    }

    public record Item(
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

        private static Item from(
                MonthlySettlementBatchSummary summary
        ) {
            return new Item(
                    summary.jobExecutionId(),
                    summary.jobInstanceId(),
                    summary.jobName(),
                    summary.status(),
                    summary.startTime(),
                    summary.endTime(),
                    summary.exitCode(),
                    summary.periodStart(),
                    summary.periodEnd()
            );
        }
    }
}