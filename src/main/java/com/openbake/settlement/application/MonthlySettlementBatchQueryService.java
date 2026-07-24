package com.openbake.settlement.application;

import com.openbake.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlySettlementBatchQueryService {

    private static final String MONTHLY_SETTLEMENT_JOB_NAME =
            "monthlySettlementJob";

    private static final int MAX_PAGE_SIZE = 100;

    private final JobRepository jobRepository;

    /** 단건 조회 */
    public MonthlySettlementBatchExecutionResult getExecution(
            Long jobExecutionId
    ) {
        validateExecutionId(jobExecutionId);

        JobExecution jobExecution;

        try {
            jobExecution =
                    jobRepository.getJobExecution(jobExecutionId);

        } catch (EmptyResultDataAccessException exception) {
            throw batchExecutionNotFound(jobExecutionId);
        }

        /*
         * JobRepository 인터페이스 계약상 조회 결과가 없으면
         * null이 반환될 수도 있으므로 두 경우를 모두 처리합니다.
         */
        if (jobExecution == null) {
            throw batchExecutionNotFound(jobExecutionId);
        }

        JobParameters jobParameters =
                jobExecution.getJobParameters();

        return new MonthlySettlementBatchExecutionResult(
                jobExecution.getId(),
                jobExecution.getJobInstance().getId(),
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus().name(),
                jobExecution.getStartTime(),
                jobExecution.getEndTime(),
                jobExecution.getExitStatus().getExitCode(),
                normalizeExitDescription(
                        jobExecution
                                .getExitStatus()
                                .getExitDescription()
                ),
                jobParameters.getString("periodStart"),
                jobParameters.getString("periodEnd")
        );
    }

    /**
     * Spring Batch 6의 JobRepository는
     * JobExplorer를 상속하며,
     * Job Instance를 최신 생성 순으로 조회하는
     * getJobInstances(jobName, start, count)와
     * 각 Instance의 마지막 실행을 조회하ㄴ는
     * getLastJobExecution(jobInstance)를 제공
     *
     * 목록 조회 */
    public MonthlySettlementBatchListResult getExecutions(
            int page,
            int size
    ) {
        validatePage(page, size);

        int start = page * size;

        /*
         * 다음 페이지가 존재하는지 확인하기 위해
         * 요청 크기보다 한 건 더 조회합니다.
         */
        List<JobInstance> jobInstances =
                jobRepository.getJobInstances(
                        MONTHLY_SETTLEMENT_JOB_NAME,
                        start,
                        size + 1
                );

        boolean hasNext = jobInstances.size() > size;

        List<MonthlySettlementBatchSummary> executions =
                jobInstances.stream()
                        .limit(size)
                        .map(jobRepository::getLastJobExecution)
                        .filter(Objects::nonNull)
                        .map(this::toSummary)
                        .toList();

        return new MonthlySettlementBatchListResult(
                executions,
                page,
                size,
                hasNext
        );
    }

    /** 단건 조회 method */
    private EntityNotFoundException batchExecutionNotFound(
            Long jobExecutionId
    ) {
        return new EntityNotFoundException(
                "배치 실행 이력을 찾을 수 없습니다. "
                        + "jobExecutionId=" + jobExecutionId
        );
    }

    private void validateExecutionId(
            Long jobExecutionId
    ) {
        /** null 안 떨어짐, 조회 결과가 0건 */
        if (jobExecutionId == null || jobExecutionId <= 0) {
            throw new IllegalArgumentException(
                    "jobExecutionId는 0보다 커야 합니다."
            );
        }
    }


    private String normalizeExitDescription(
            String exitDescription
    ) {
        if (exitDescription == null
                || exitDescription.isBlank()) {
            return null;
        }

        /*
         * 실패 시 전체 스택 트레이스가 응답에 노출되는 것을 막습니다.
         * 상세 오류는 서버 로그와 Batch 테이블에서 확인합니다.
         */
        int maxLength = 500;

        if (exitDescription.length() <= maxLength) {
            return exitDescription;
        }

        return exitDescription.substring(0, maxLength);
    }

    /** 목록 조회 method */
    private MonthlySettlementBatchSummary toSummary(
            JobExecution jobExecution
    ) {
        JobParameters parameters =
                jobExecution.getJobParameters();

        return new MonthlySettlementBatchSummary(
                jobExecution.getId(),
                jobExecution.getJobInstance().getId(),
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus().name(),
                jobExecution.getStartTime(),
                jobExecution.getEndTime(),
                jobExecution.getExitStatus().getExitCode(),
                parameters.getString("periodStart"),
                parameters.getString("periodEnd")
        );
    }

    private void validatePage(
            int page,
            int size
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "page는 0 이상이어야 합니다."
            );
        }

        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "size는 1 이상 100 이하여야 합니다."
            );
        }
    }
}