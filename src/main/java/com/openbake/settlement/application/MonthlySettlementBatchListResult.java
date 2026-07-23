package com.openbake.settlement.application;

import java.util.List;

public record MonthlySettlementBatchListResult(
        List<MonthlySettlementBatchSummary> executions,
        int page,
        int size,
        boolean hasNext
) {
}