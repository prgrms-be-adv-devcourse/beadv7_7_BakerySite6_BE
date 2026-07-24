package com.openbake.settlement.infrastructure.batch;

import com.openbake.settlement.application.MonthlySettlementResult;
import com.openbake.settlement.application.MonthlySettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 월 정산 서비스를 실행하는 Spring Batch Tasklet입니다.
 *
 * JobParameter:
 * - periodStart: 정산 시작일, 포함
 * - periodEnd: 정산 종료일, 미포함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlySettlementTasklet implements Tasklet {

    private static final String PERIOD_START_PARAMETER =
            "periodStart";

    private static final String PERIOD_END_PARAMETER =
            "periodEnd";

    private final MonthlySettlementService monthlySettlementService;

    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext
    ) throws Exception {
        StepExecution stepExecution =
                contribution.getStepExecution();

        JobParameters jobParameters =
                stepExecution
                        .getJobExecution()
                        .getJobParameters();

        String periodStartValue =
                requireParameter(
                        jobParameters,
                        PERIOD_START_PARAMETER
                );

        String periodEndValue =
                requireParameter(
                        jobParameters,
                        PERIOD_END_PARAMETER
                );

        LocalDate periodStart =
                parseDate(
                        PERIOD_START_PARAMETER,
                        periodStartValue
                );

        LocalDate periodEnd =
                parseDate(
                        PERIOD_END_PARAMETER,
                        periodEndValue
                );

        log.info(
                "월 정산 배치를 시작합니다. periodStart={}, periodEnd={}",
                periodStart,
                periodEnd
        );

        MonthlySettlementResult result =
                monthlySettlementService.settle(
                        periodStart,
                        periodEnd
                );

        log.info(
                "월 정산 배치를 완료했습니다. "
                        + "settlementCount={}, targetCount={}, "
                        + "totalPayoutAmount={}",
                result.settlementCount(),
                result.targetCount(),
                result.totalPayoutAmount()
        );

        return RepeatStatus.FINISHED;
    }

    private String requireParameter(
            JobParameters jobParameters,
            String parameterName
    ) {
        String value =
                jobParameters.getString(parameterName);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "필수 JobParameter가 없습니다. parameter="
                            + parameterName
            );
        }

        return value.trim();
    }

    private LocalDate parseDate(
            String parameterName,
            String value
    ) {
        try {
            return LocalDate.parse(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "JobParameter의 날짜 형식이 올바르지 않습니다. "
                            + "parameter=" + parameterName
                            + ", value=" + value
                            + ", expectedFormat=yyyy-MM-dd",
                    exception
            );
        }
    }
}