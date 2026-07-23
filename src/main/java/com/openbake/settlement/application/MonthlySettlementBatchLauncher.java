package com.openbake.settlement.application;

import com.openbake.common.exception.BusinessException;
import com.openbake.common.exception.ErrorCode;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 매월 1일 새벽 2시
 * → 직전 달의 시작일과 이번 달 시작일 계산
 * → MonthlySettlementBatchLauncher 실행
 * → Spring Batch 실행 이력 저장
 * → 판매자별 월 정산 생성
 *
 * MonthlySettlementBatchLauncher가
 * 날짜를 받아 monthlySettlementJob을 실행하도록 구현돼 있으므로
 * 스케줄러에서는 이 클래스를 그대로 호출
 *
 * */
@Service
public class MonthlySettlementBatchLauncher {

    private final JobOperator jobOperator;
    private final Job monthlySettlementJob;

    public MonthlySettlementBatchLauncher(
            JobOperator jobOperator,
            @Qualifier("monthlySettlementJob")
            Job monthlySettlementJob
    ) {
        this.jobOperator = jobOperator;
        this.monthlySettlementJob = monthlySettlementJob;
    }

    public JobExecution launch(
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        validatePeriod(periodStart, periodEnd);

        JobParameters jobParameters =
                new JobParametersBuilder()
                        .addString(
                                "periodStart",
                                periodStart.toString()
                        )
                        .addString(
                                "periodEnd",
                                periodEnd.toString()
                        )
                        .toJobParameters();

        try {
            return jobOperator.start(
                    monthlySettlementJob,
                    jobParameters
            );
        } catch (JobInstanceAlreadyCompleteException exception) {
            throw new BusinessException(
                    ErrorCode.SETTLEMENT_BATCH_ALREADY_COMPLETED
            );

        } catch (JobExecutionAlreadyRunningException exception) {
            throw new BusinessException(
                    ErrorCode.SETTLEMENT_BATCH_ALREADY_RUNNING
            );

        } catch (JobRestartException exception) {
            throw new BusinessException(
                    ErrorCode.SETTLEMENT_BATCH_RESTART_FAILED
            );

        } catch (InvalidJobParametersException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_SETTLEMENT_BATCH_PARAMETERS
            );

        } catch (Exception exception) {
            throw new BusinessException(
                    ErrorCode.SETTLEMENT_BATCH_EXECUTION_FAILED
            );
        }
    }

    private void validatePeriod(
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        Objects.requireNonNull(
                periodStart,
                "periodStart는 필수입니다."
        );

        Objects.requireNonNull(
                periodEnd,
                "periodEnd는 필수입니다."
        );

        if (!periodStart.isBefore(periodEnd)) {
            throw new IllegalArgumentException(
                    "periodStart는 periodEnd보다 이전이어야 합니다."
            );
        }
    }
}