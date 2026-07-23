package com.openbake.settlement.infrastructure.batch;

import com.openbake.common.exception.BusinessException;
import com.openbake.common.exception.ErrorCode;
import com.openbake.settlement.application.MonthlySettlementBatchLauncher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 매월 직전 달의 정산 배치를 자동 실행합니다.
 *
 * 같은 Job 이름과 동일한 식별 파라미터로
 * 이미 성공한 Job을 다시 실행하면
 * Spring Batch는 완료된 Job Instance로 판단해
 * 중복 실행을 막습니다.
 * 성공한 Job Instance의 수명은
 * 첫 성공 실행으로 완료됩니다.
 *
 * 예:
 * 2026-08-01 실행
 * → 2026-07-01 이상
 * → 2026-08-01 미만
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlySettlementScheduler {

    private static final ZoneId SETTLEMENT_ZONE =
            ZoneId.of("Asia/Seoul");

    private final MonthlySettlementBatchLauncher
            monthlySettlementBatchLauncher;

    /**
     * 기본 설정은 비활성화 상태입니다.
     *
     * application.yml 또는 환경별 설정에서
     * settlement.batch.monthly.cron 값을 지정하면 활성화됩니다.
     * 관리자 수동 실행
     * POST /internal/v1/settlement-batches/monthly
     *
     * 자동 실행
     * 매월 1일 오후 3시 (프로그래머스 aws사용시간 있음)
     * @Scheduled
     */
    @Scheduled(
            cron = "${settlement.batch.monthly.cron:-}",
            zone = "${settlement.batch.monthly.zone:Asia/Seoul}"
    )
    public void runPreviousMonthSettlement() {
        LocalDate today =
                LocalDate.now(SETTLEMENT_ZONE);

        LocalDate periodEnd =
                today.withDayOfMonth(1);

        LocalDate periodStart =
                periodEnd.minusMonths(1);

        log.info(
                "월 정산 스케줄러를 시작합니다. "
                        + "periodStart={}, periodEnd={}",
                periodStart,
                periodEnd
        );

        try {
            JobExecution jobExecution =
                    monthlySettlementBatchLauncher.launch(
                            periodStart,
                            periodEnd
                    );

            log.info(
                    "월 정산 스케줄러 실행을 완료했습니다. "
                            + "jobExecutionId={}, status={}",
                    jobExecution.getId(),
                    jobExecution.getStatus()
            );

        } catch (BusinessException exception) {
            if (exception.getErrorCode()
                    == ErrorCode.SETTLEMENT_BATCH_ALREADY_COMPLETED) {

                log.warn(
                        "동일 기간의 월 정산 배치가 이미 완료됐습니다. "
                                + "periodStart={}, periodEnd={}",
                        periodStart,
                        periodEnd
                );

                return;
            }

            log.error(
                    "월 정산 스케줄러 실행에 실패했습니다. "
                            + "code={}, periodStart={}, periodEnd={}",
                    exception.getErrorCode().getCode(),
                    periodStart,
                    periodEnd,
                    exception
            );

        } catch (Exception exception) {
            /*
             * 예외를 숨기지는 않되 스케줄러 스레드가
             * 비정상 종료되지 않도록 로그로 기록합니다.
             */
            log.error(
                    "월 정산 스케줄러 실행에 실패했습니다. "
                            + "periodStart={}, periodEnd={}",
                    periodStart,
                    periodEnd,
                    exception
            );
        }
    }
}