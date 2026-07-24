package com.openbake.settlement.presentation;

import com.openbake.common.response.ApiResponse;
import com.openbake.settlement.application
        .MonthlySettlementBatchExecutionResult;
import com.openbake.settlement.application
        .MonthlySettlementBatchLauncher;
import com.openbake.settlement.application
        .MonthlySettlementBatchQueryService;
import com.openbake.settlement.application.MonthlySettlementBatchListResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/internal/v1/settlement-batches")
@RequiredArgsConstructor
public class MonthlySettlementBatchController {

    private final MonthlySettlementBatchLauncher
            monthlySettlementBatchLauncher;

    private final MonthlySettlementBatchQueryService
            monthlySettlementBatchQueryService;

    @PostMapping("/monthly")
    public ApiResponse<MonthlySettlementBatchResponse>
    runMonthlySettlement(
            @Valid
            @RequestBody MonthlySettlementBatchRequest request
    ) {
        JobExecution jobExecution =
                monthlySettlementBatchLauncher.launch(
                        request.periodStart(),
                        request.periodEnd()
                );

        MonthlySettlementBatchResponse response =
                new MonthlySettlementBatchResponse(
                        jobExecution.getId(),
                        jobExecution
                                .getJobInstance()
                                .getJobName(),
                        jobExecution
                                .getStatus()
                                .name()
                );

        return ApiResponse.ok(response);
    }

    @GetMapping("/{jobExecutionId}")
    public ApiResponse<MonthlySettlementBatchExecutionResponse>
    getBatchExecution(
            @PathVariable Long jobExecutionId
    ) {
        MonthlySettlementBatchExecutionResult result =
                monthlySettlementBatchQueryService
                        .getExecution(jobExecutionId);

        return ApiResponse.ok(
                MonthlySettlementBatchExecutionResponse.from(
                        result
                )
        );
    }

    /** 위치 주의 */
    @GetMapping
    public ApiResponse<MonthlySettlementBatchListResponse>
    getBatchExecutions(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {
        MonthlySettlementBatchListResult result =
                monthlySettlementBatchQueryService.getExecutions(
                        page,
                        size
                );

        return ApiResponse.ok(
                MonthlySettlementBatchListResponse.from(result)
        );
    }
}