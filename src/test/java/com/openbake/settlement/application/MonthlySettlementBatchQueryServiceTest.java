package com.openbake.settlement.application;

import com.openbake.common.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.dao.EmptyResultDataAccessException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MonthlySettlementBatchQueryServiceTest {

    @Mock
    private JobRepository jobRepository;

    private MonthlySettlementBatchQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService =
                new MonthlySettlementBatchQueryService(
                        jobRepository
                );
    }

    @Test
    @DisplayName("배치 실행 ID로 실행 이력을 조회한다")
    void getExecution() {
        // given
        JobParameters parameters =
                new JobParametersBuilder()
                        .addString(
                                "periodStart",
                                "2026-07-01"
                        )
                        .addString(
                                "periodEnd",
                                "2026-08-01"
                        )
                        .toJobParameters();

        JobInstance jobInstance =
                new JobInstance(
                        10L,
                        "monthlySettlementJob"
                );

        JobExecution jobExecution =
                new JobExecution(
                        1L,
                        jobInstance,
                        parameters
                );

        LocalDateTime startTime =
                LocalDateTime.of(
                        2026,
                        8,
                        1,
                        2,
                        0
                );

        LocalDateTime endTime =
                LocalDateTime.of(
                        2026,
                        8,
                        1,
                        2,
                        0,
                        5
                );

        jobExecution.setStatus(BatchStatus.COMPLETED);
        jobExecution.setStartTime(startTime);
        jobExecution.setEndTime(endTime);
        jobExecution.setExitStatus(ExitStatus.COMPLETED);

        when(jobRepository.getJobExecution(1L))
                .thenReturn(jobExecution);

        // when
        MonthlySettlementBatchExecutionResult result =
                queryService.getExecution(1L);

        // then
        assertThat(result.jobExecutionId())
                .isEqualTo(1L);

        assertThat(result.jobInstanceId())
                .isEqualTo(10L);

        assertThat(result.jobName())
                .isEqualTo("monthlySettlementJob");

        assertThat(result.status())
                .isEqualTo("COMPLETED");

        assertThat(result.startTime())
                .isEqualTo(startTime);

        assertThat(result.endTime())
                .isEqualTo(endTime);

        assertThat(result.exitCode())
                .isEqualTo("COMPLETED");

        assertThat(result.periodStart())
                .isEqualTo("2026-07-01");

        assertThat(result.periodEnd())
                .isEqualTo("2026-08-01");
    }

    @Test
    @DisplayName("배치 실행 조회 결과가 없으면 EntityNotFoundException으로 변환한다")
    void rejectEmptyExecutionResult() {
        // given
        when(jobRepository.getJobExecution(199L))
                .thenThrow(
                        new EmptyResultDataAccessException(1)
                );

        // when & then
        assertThatThrownBy(() ->
                queryService.getExecution(199L)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(
                        "배치 실행 이력을 찾을 수 없습니다. "
                                + "jobExecutionId=199"
                );
    }

    @Test
    @DisplayName("배치 실행 ID는 0보다 커야 한다")
    void rejectInvalidExecutionId() {
        assertThatThrownBy(() ->
                queryService.getExecution(0L)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "jobExecutionId는 0보다 커야 합니다."
                );

        verify(jobRepository, never())
                .getJobExecution(0L);
    }

    @Test
    @DisplayName("월 정산 배치 실행 이력을 최신 순으로 조회한다")
    void getExecutions() {
        // given
        JobInstance firstInstance =
                new JobInstance(
                        20L,
                        "monthlySettlementJob"
                );

        JobInstance secondInstance =
                new JobInstance(
                        10L,
                        "monthlySettlementJob"
                );

        JobExecution firstExecution =
                createJobExecution(
                        200L,
                        firstInstance,
                        "2026-08-01",
                        "2026-09-01"
                );

        JobExecution secondExecution =
                createJobExecution(
                        100L,
                        secondInstance,
                        "2026-07-01",
                        "2026-08-01"
                );

        when(jobRepository.getJobInstances(
                "monthlySettlementJob",
                0,
                3
        )).thenReturn(List.of(
                firstInstance,
                secondInstance
        ));

        when(jobRepository.getLastJobExecution(firstInstance))
                .thenReturn(firstExecution);

        when(jobRepository.getLastJobExecution(secondInstance))
                .thenReturn(secondExecution);

        // when
        MonthlySettlementBatchListResult result =
                queryService.getExecutions(0, 2);

        // then
        assertThat(result.executions())
                .hasSize(2);

        assertThat(result.executions().get(0).jobExecutionId())
                .isEqualTo(200L);

        assertThat(result.executions().get(0).periodStart())
                .isEqualTo("2026-08-01");

        assertThat(result.executions().get(1).jobExecutionId())
                .isEqualTo(100L);

        assertThat(result.page())
                .isZero();

        assertThat(result.size())
                .isEqualTo(2);

        assertThat(result.hasNext())
                .isFalse();
    }

    @Test
    @DisplayName("조회 크기보다 한 건 더 있으면 다음 페이지가 존재한다")
    void returnHasNext() {
        // given
        JobInstance instance1 =
                new JobInstance(30L, "monthlySettlementJob");

        JobInstance instance2 =
                new JobInstance(20L, "monthlySettlementJob");

        JobInstance instance3 =
                new JobInstance(10L, "monthlySettlementJob");

        when(jobRepository.getJobInstances(
                "monthlySettlementJob",
                0,
                3
        )).thenReturn(List.of(
                instance1,
                instance2,
                instance3
        ));

        when(jobRepository.getLastJobExecution(instance1))
                .thenReturn(
                        createJobExecution(
                                300L,
                                instance1,
                                "2026-09-01",
                                "2026-10-01"
                        )
                );

        when(jobRepository.getLastJobExecution(instance2))
                .thenReturn(
                        createJobExecution(
                                200L,
                                instance2,
                                "2026-08-01",
                                "2026-09-01"
                        )
                );

        // when
        MonthlySettlementBatchListResult result =
                queryService.getExecutions(0, 2);

        // then
        assertThat(result.executions())
                .hasSize(2);

        assertThat(result.hasNext())
                .isTrue();
    }

    @Test
    @DisplayName("페이지 번호는 0 이상이어야 한다")
    void rejectNegativePage() {
        assertThatThrownBy(() ->
                queryService.getExecutions(-1, 20)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("page는 0 이상이어야 합니다.");

        verifyNoInteractions(jobRepository);
    }

    @Test
    @DisplayName("조회 크기는 100을 초과할 수 없다")
    void rejectTooLargeSize() {
        assertThatThrownBy(() ->
                queryService.getExecutions(0, 101)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "size는 1 이상 100 이하여야 합니다."
                );

        verifyNoInteractions(jobRepository);
    }

    /** 헬퍼 메서드 */
    private JobExecution createJobExecution(
            Long executionId,
            JobInstance jobInstance,
            String periodStart,
            String periodEnd
    ) {
        JobParameters parameters =
                new JobParametersBuilder()
                        .addString(
                                "periodStart",
                                periodStart
                        )
                        .addString(
                                "periodEnd",
                                periodEnd
                        )
                        .toJobParameters();

        JobExecution execution =
                new JobExecution(
                        executionId,
                        jobInstance,
                        parameters
                );

        execution.setStatus(BatchStatus.COMPLETED);
        execution.setExitStatus(ExitStatus.COMPLETED);

        return execution;
    }
}