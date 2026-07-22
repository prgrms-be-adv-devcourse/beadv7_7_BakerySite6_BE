package com.openbake.settlement.presentation;

import com.openbake.settlement.application.MonthlySettlementBatchLauncher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/settlement-batches")
@RequiredArgsConstructor
public class MonthlySettlementBatchController {

    private final MonthlySettlementBatchLauncher
            monthlySettlementBatchLauncher;

    @PostMapping("/monthly")
    public ResponseEntity<MonthlySettlementBatchResponse>
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

        return ResponseEntity.ok(response);
    }
}