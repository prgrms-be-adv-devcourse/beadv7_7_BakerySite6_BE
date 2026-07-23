package com.openbake.settlement.application;

import com.openbake.common.exception.BusinessException;
import com.openbake.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonthlySettlementBatchLauncherTest {

    private static final LocalDate PERIOD_START =
            LocalDate.of(2026, 7, 1);

    private static final LocalDate PERIOD_END =
            LocalDate.of(2026, 8, 1);

    @Mock
    private JobOperator jobOperator;

    @Mock
    private Job monthlySettlementJob;

    private MonthlySettlementBatchLauncher launcher;

    @BeforeEach
    void setUp() {
        launcher = new MonthlySettlementBatchLauncher(
                jobOperator,
                monthlySettlementJob
        );
    }

    @Test
    @DisplayName("정산 기간을 JobParameter로 전달하여 배치를 실행한다")
    void launchMonthlySettlementJob() throws Exception {
        // given
        JobExecution expectedExecution =
                mock(JobExecution.class);

        when(jobOperator.start(
                same(monthlySettlementJob),
                any(JobParameters.class)
        )).thenReturn(expectedExecution);

        ArgumentCaptor<JobParameters> parametersCaptor =
                ArgumentCaptor.forClass(JobParameters.class);

        // when
        JobExecution actualExecution =
                launcher.launch(
                        PERIOD_START,
                        PERIOD_END
                );

        // then
        assertThat(actualExecution)
                .isSameAs(expectedExecution);

        verify(jobOperator).start(
                same(monthlySettlementJob),
                parametersCaptor.capture()
        );

        JobParameters parameters =
                parametersCaptor.getValue();

        assertThat(parameters.getString("periodStart"))
                .isEqualTo("2026-07-01");

        assertThat(parameters.getString("periodEnd"))
                .isEqualTo("2026-08-01");
    }

    @Test
    @DisplayName("동일 기간의 완료된 Job은 정산 배치 완료 예외로 변환한다")
    void mapAlreadyCompleteException() throws Exception {
        // given
        JobInstanceAlreadyCompleteException batchException =
                mock(JobInstanceAlreadyCompleteException.class);

        when(jobOperator.start(
                same(monthlySettlementJob),
                any(JobParameters.class)
        )).thenThrow(batchException);

        // when & then
        assertBusinessException(
                ErrorCode.SETTLEMENT_BATCH_ALREADY_COMPLETED
        );
    }

    @Test
    @DisplayName("동일 기간의 Job이 실행 중이면 실행 중 예외로 변환한다")
    void mapAlreadyRunningException() throws Exception {
        // given
        JobExecutionAlreadyRunningException batchException =
                mock(JobExecutionAlreadyRunningException.class);

        when(jobOperator.start(
                same(monthlySettlementJob),
                any(JobParameters.class)
        )).thenThrow(batchException);

        // when & then
        assertBusinessException(
                ErrorCode.SETTLEMENT_BATCH_ALREADY_RUNNING
        );
    }

    @Test
    @DisplayName("Job을 재시작할 수 없으면 재시작 실패 예외로 변환한다")
    void mapRestartException() throws Exception {
        // given
        JobRestartException batchException =
                mock(JobRestartException.class);

        when(jobOperator.start(
                same(monthlySettlementJob),
                any(JobParameters.class)
        )).thenThrow(batchException);

        // when & then
        assertBusinessException(
                ErrorCode.SETTLEMENT_BATCH_RESTART_FAILED
        );
    }

    @Test
    @DisplayName("JobParameter가 잘못되면 파라미터 오류로 변환한다")
    void mapInvalidJobParametersException() throws Exception {
        // given
        InvalidJobParametersException batchException =
                mock(InvalidJobParametersException.class);

        when(jobOperator.start(
                same(monthlySettlementJob),
                any(JobParameters.class)
        )).thenThrow(batchException);

        // when & then
        assertBusinessException(
                ErrorCode.INVALID_SETTLEMENT_BATCH_PARAMETERS
        );
    }

    @Test
    @DisplayName("예상하지 못한 배치 오류는 실행 실패 예외로 변환한다")
    void mapUnexpectedException() throws Exception {
        // given
        when(jobOperator.start(
                same(monthlySettlementJob),
                any(JobParameters.class)
        )).thenThrow(new RuntimeException("DB 연결 실패"));

        // when & then
        assertBusinessException(
                ErrorCode.SETTLEMENT_BATCH_EXECUTION_FAILED
        );
    }

    @Test
    @DisplayName("시작일과 종료일이 같으면 Job을 실행하지 않는다")
    void rejectSamePeriod() {
        assertThatThrownBy(() ->
                launcher.launch(
                        PERIOD_START,
                        PERIOD_START
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "periodStart는 periodEnd보다 이전이어야 합니다."
                );

        verifyNoInteractions(jobOperator);
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦으면 Job을 실행하지 않는다")
    void rejectReversedPeriod() {
        assertThatThrownBy(() ->
                launcher.launch(
                        PERIOD_END,
                        PERIOD_START
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "periodStart는 periodEnd보다 이전이어야 합니다."
                );

        verifyNoInteractions(jobOperator);
    }

    @Test
    @DisplayName("시작일이 null이면 Job을 실행하지 않는다")
    void rejectNullPeriodStart() {
        assertThatThrownBy(() ->
                launcher.launch(
                        null,
                        PERIOD_END
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("periodStart는 필수입니다.");

        verifyNoInteractions(jobOperator);
    }

    private void assertBusinessException(
            ErrorCode expectedErrorCode
    ) {
        assertThatThrownBy(() ->
                launcher.launch(
                        PERIOD_START,
                        PERIOD_END
                )
        )
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> {
                            assertThat(exception.getErrorCode())
                                    .isEqualTo(expectedErrorCode);

                            assertThat(exception.getMessage())
                                    .isEqualTo(
                                            expectedErrorCode.getMessage()
                                    );
                        }
                );
    }
}