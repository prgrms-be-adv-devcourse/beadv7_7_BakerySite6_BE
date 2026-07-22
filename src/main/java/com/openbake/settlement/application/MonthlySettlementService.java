package com.openbake.settlement.application;

import com.openbake.settlement.domain.Settlement;
import com.openbake.settlement.domain.SettlementLine;
import com.openbake.settlement.domain.SettlementLineRepository;
import com.openbake.settlement.domain.SettlementRepository;
import com.openbake.settlement.domain.SettlementTarget;
import com.openbake.settlement.domain.SettlementTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 특정 기간의 정산 대상을 판매자별로 집계해
 * 월 정산서를 생성하는 애플리케이션 서비스입니다.
 *
 * | 정산 대상    |  총 판매금액 |    수수료 |   순정산금액 |
 * | -------- | ------: | -----: | ------: |
 * | Target 1 | 30,000원 | 3,000원 | 27,000원 |
 * | Target 2 | 20,000원 | 2,000원 | 18,000원 |
 *
 * 생성되는 Settlement
 * grossSalesAmount = 50,000원
 * commissionAmount = 5,000원
 * netSalesAmount = 45,000원
 * adjustmentAmount = 0원
 * payoutAmount = 45,000원
 * targetCount = 2
 * status = READY
 *
 * 그리고 각 Target은 다음 상태로 변경
 * status: PENDING → ASSIGNED
 * settlementId: null → 생성된 Settlement ID
 *
 * 추후 Spring Batch의 Tasklet 또는 Writer에서 이 서비스를 호출합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MonthlySettlementService {

    /**
     * 현재 서비스의 기준 시간대입니다.
     *
     * 한국 서비스이므로 구매확정 기간을 Asia/Seoul 기준으로 계산합니다.
     * 추후 application.yml 설정값으로 분리할 수 있습니다.
     */
    private static final ZoneId SETTLEMENT_ZONE =
            ZoneId.of("Asia/Seoul");

    private static final BigDecimal ZERO_MONEY =
            new BigDecimal("0.00");

    private final SettlementTargetRepository settlementTargetRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementLineRepository settlementLineRepository;
    /**
     * Settlement 저장
     * SettlementLine 저장
     * SettlementTarget 상태 변경
     */
    /**
     * 지정한 기간의 PENDING 정산 대상을 판매자별로 정산합니다.
     *
     * 기간은 시작일 포함, 종료일 미포함입니다.
     *
     * 예:
     * periodStart = 2026-07-01
     * periodEnd   = 2026-08-01
     *
     * 처리 범위:
     * 2026-07-01 00:00:00 이상
     * 2026-08-01 00:00:00 미만
     */
    public MonthlySettlementResult settle(
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        validatePeriod(periodStart, periodEnd);

        OffsetDateTime confirmedFrom =
                periodStart
                        .atStartOfDay(SETTLEMENT_ZONE)
                        .toOffsetDateTime();

        OffsetDateTime confirmedTo =
                periodEnd
                        .atStartOfDay(SETTLEMENT_ZONE)
                        .toOffsetDateTime();

        List<SettlementTarget> pendingTargets =
                settlementTargetRepository.findAllPendingTargets(
                        confirmedFrom,
                        confirmedTo
                );

        if (pendingTargets.isEmpty()) {
            return MonthlySettlementResult.empty(
                    periodStart,
                    periodEnd
            );
        }

        Map<Long, List<SettlementTarget>> targetsBySeller =
                groupBySeller(pendingTargets);

        int createdSettlementCount = 0;
        int assignedTargetCount = 0;
        BigDecimal totalPayoutAmount = ZERO_MONEY;

        for (Map.Entry<Long, List<SettlementTarget>> entry
                : targetsBySeller.entrySet()) {

            Long sellerId = entry.getKey();
            List<SettlementTarget> sellerTargets = entry.getValue();

            validateSettlementNotExists(
                    sellerId,
                    periodStart,
                    periodEnd
            );

            Settlement settlement = createSettlement(
                    sellerId,
                    periodStart,
                    periodEnd,
                    sellerTargets
            );

            Settlement savedSettlement =
                    settlementRepository.save(settlement);

            Long settlementId = savedSettlement.getId();

            if (settlementId == null) {
                throw new IllegalStateException(
                        "저장된 Settlement의 ID가 없습니다."
                );
            }

            List<SettlementLine> settlementLines =
                    createSettlementLines(
                            settlementId,
                            sellerTargets
                    );

            settlementLineRepository.saveAll(settlementLines);

            assignTargets(
                    settlementId,
                    sellerTargets
            );

            settlementTargetRepository.saveAll(sellerTargets);

            createdSettlementCount++;
            assignedTargetCount += sellerTargets.size();

            totalPayoutAmount = totalPayoutAmount.add(
                    savedSettlement.getPayoutAmount()
            );
        }

        return new MonthlySettlementResult(
                periodStart,
                periodEnd,
                createdSettlementCount,
                assignedTargetCount,
                totalPayoutAmount.setScale(
                        2,
                        RoundingMode.UNNECESSARY
                )
        );
    }

    private Map<Long, List<SettlementTarget>> groupBySeller(
            List<SettlementTarget> targets
    ) {
        Map<Long, List<SettlementTarget>> targetsBySeller =
                new LinkedHashMap<>();

        for (SettlementTarget target : targets) {
            targetsBySeller
                    .computeIfAbsent(
                            target.getSellerId(),
                            ignored -> new ArrayList<>()
                    )
                    .add(target);
        }

        return targetsBySeller;
    }

    private Settlement createSettlement(
            Long sellerId,
            LocalDate periodStart,
            LocalDate periodEnd,
            List<SettlementTarget> targets
    ) {
        BigDecimal grossSalesAmount = targets.stream()
                .map(SettlementTarget::getGrossAmount)
                .reduce(ZERO_MONEY, BigDecimal::add)
                .setScale(2, RoundingMode.UNNECESSARY);

        BigDecimal commissionAmount = targets.stream()
                .map(SettlementTarget::getCommissionAmount)
                .reduce(ZERO_MONEY, BigDecimal::add)
                .setScale(2, RoundingMode.UNNECESSARY);

        /*
         * 아직 SettlementAdjustment 구현 전이므로
         * 이번 단계에서는 보정 금액을 0원으로 생성합니다.
         */
        BigDecimal adjustmentAmount = ZERO_MONEY;

        return Settlement.create(
                sellerId,
                periodStart,
                periodEnd,
                grossSalesAmount,
                commissionAmount,
                adjustmentAmount,
                targets.size()
        );
    }

    private List<SettlementLine> createSettlementLines(
            Long settlementId,
            List<SettlementTarget> targets
    ) {
        return targets.stream()
                .map(target -> SettlementLine.from(
                        settlementId,
                        target
                ))
                .toList();
    }

    private void assignTargets(
            Long settlementId,
            List<SettlementTarget> targets
    ) {
        targets.forEach(
                target -> target.assignTo(settlementId)
        );
    }

    private void validateSettlementNotExists(
            Long sellerId,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        boolean exists =
                settlementRepository
                        .existsBySellerIdAndPeriodStartAndPeriodEnd(
                                sellerId,
                                periodStart,
                                periodEnd
                        );

        if (exists) {
            throw new IllegalStateException(
                    "동일 판매자와 정산 기간의 정산서가 이미 존재합니다. "
                            + "sellerId=" + sellerId
                            + ", periodStart=" + periodStart
                            + ", periodEnd=" + periodEnd
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