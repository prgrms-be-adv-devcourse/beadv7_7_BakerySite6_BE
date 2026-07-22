package com.openbake.settlement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 판매자 한 명의 특정 기간에 대한 월 정산 결과입니다.
 *
 * 개별 정산 상세는 SettlementLine으로 관리하고,
 * 이 엔티티는 정산 기간과 합계 금액, 지급 상태를 관리합니다.
 */
@Getter
@Entity
@Table(
        name = "settlements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlement_seller_period",
                        columnNames = {
                                "seller_id",
                                "period_start",
                                "period_end"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_settlement_seller_period",
                        columnList = "seller_id, period_start, period_end"
                ),
                @Index(
                        name = "idx_settlement_status_created",
                        columnList = "status, created_at"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    /**
     * 정산 대상 기간 시작일입니다.
     *
     * 포함 범위로 사용합니다.
     */
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    /**
     * 정산 대상 기간 종료일입니다.
     *
     * 미포함 범위로 사용하는 것을 권장합니다.
     *
     * 예:
     * 7월 정산
     * periodStart = 2026-07-01
     * periodEnd   = 2026-08-01
     */
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    /**
     * 수수료 차감 전 총 판매금액입니다.
     */
    @Column(
            name = "gross_sales_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal grossSalesAmount;

    /**
     * 총 서비스 수수료입니다.
     */
    @Column(
            name = "commission_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal commissionAmount;

    /**
     * 수수료 차감 후 기본 정산금액입니다.
     */
    @Column(
            name = "net_sales_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal netSalesAmount;

    /**
     * 환불, 관리자 보정 등의 추가·차감 금액입니다.
     *
     * 양수: 추가 지급
     * 음수: 차감
     */
    @Column(
            name = "adjustment_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal adjustmentAmount;

    /**
     * 판매자에게 실제 지급할 예정 금액입니다.
     *
     * netSalesAmount + adjustmentAmount
     */
    @Column(
            name = "payout_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal payoutAmount;

    /**
     * 정산서에 포함된 SettlementTarget 건수입니다.
     */
    @Column(name = "target_count", nullable = false)
    private Integer targetCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SettlementStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * 판매자 지급까지 완료된 시각입니다.
     */
    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    private Settlement(
            Long sellerId,
            LocalDate periodStart,
            LocalDate periodEnd,
            BigDecimal grossSalesAmount,
            BigDecimal commissionAmount,
            BigDecimal adjustmentAmount,
            Integer targetCount
    ) {
        validate(
                sellerId,
                periodStart,
                periodEnd,
                grossSalesAmount,
                commissionAmount,
                adjustmentAmount,
                targetCount
        );
     /** 총 판매금액       500,000원
      *  총 수수료          50,000원
      *  기본 순정산금액    450,000원
      *  보정금액           -10,000원 adjustmentAmount는 반드시 값이 있어야 합니다.
      *  최종 지급금액      440,000원
      */

        BigDecimal normalizedGrossSalesAmount =
                normalizeMoney(grossSalesAmount);

        BigDecimal normalizedCommissionAmount =
                normalizeMoney(commissionAmount);

        BigDecimal normalizedAdjustmentAmount =
                normalizeMoney(adjustmentAmount);

        BigDecimal calculatedNetSalesAmount =
                normalizedGrossSalesAmount
                        .subtract(normalizedCommissionAmount)
                        .setScale(2, RoundingMode.UNNECESSARY);

        BigDecimal calculatedPayoutAmount =
                calculatedNetSalesAmount
                        .add(normalizedAdjustmentAmount)
                        .setScale(2, RoundingMode.UNNECESSARY);

        if (calculatedNetSalesAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "수수료는 총 판매금액을 초과할 수 없습니다."
            );
        }

        if (calculatedPayoutAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "최종 지급 예정 금액은 0보다 작을 수 없습니다."
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        this.sellerId = sellerId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.grossSalesAmount = normalizedGrossSalesAmount;
        this.commissionAmount = normalizedCommissionAmount;
        this.netSalesAmount = calculatedNetSalesAmount;
        this.adjustmentAmount = normalizedAdjustmentAmount;
        this.payoutAmount = calculatedPayoutAmount;
        this.targetCount = targetCount;
        this.status = SettlementStatus.READY;
        this.createdAt = now;
        this.updatedAt = now;
        this.completedAt = null;
    }

    /**
     * 월 정산 배치에서 집계가 완료된 정산서를 생성합니다.
     */
    public static Settlement create(
            Long sellerId,
            LocalDate periodStart,
            LocalDate periodEnd,
            BigDecimal grossSalesAmount,
            BigDecimal commissionAmount,
            BigDecimal adjustmentAmount,
            Integer targetCount
    ) {
        return new Settlement(
                sellerId,
                periodStart,
                periodEnd,
                grossSalesAmount,
                commissionAmount,
                adjustmentAmount,
                targetCount
        );
    }

    /**
     * 판매자 지급을 시작합니다.
     */
    public void startPaying() {
        if (status != SettlementStatus.READY
                && status != SettlementStatus.FAILED) {
            throw new IllegalStateException(
                    "READY 또는 FAILED 상태에서만 지급을 시작할 수 있습니다."
            );
        }

        this.status = SettlementStatus.PAYING;
        touch();
    }

    /**
     * 지급 실패 상태로 변경합니다.
     */
    public void failPayment() {
        if (status != SettlementStatus.PAYING) {
            throw new IllegalStateException(
                    "PAYING 상태에서만 지급 실패 처리할 수 있습니다."
            );
        }

        this.status = SettlementStatus.FAILED;
        touch();
    }

    /**
     * 판매자 지급까지 완료합니다.
     */
    public void complete() {
        if (status != SettlementStatus.PAYING) {
            throw new IllegalStateException(
                    "PAYING 상태에서만 정산을 완료할 수 있습니다."
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        this.status = SettlementStatus.COMPLETED;
        this.completedAt = now;
        this.updatedAt = now;
    }

    /**
     * 정산 지급을 보류합니다.
     */
    public void hold() {
        if (status == SettlementStatus.COMPLETED) {
            throw new IllegalStateException(
                    "완료된 정산은 보류할 수 없습니다."
            );
        }

        if (status == SettlementStatus.PAYING) {
            throw new IllegalStateException(
                    "지급 진행 중인 정산은 보류할 수 없습니다."
            );
        }

        this.status = SettlementStatus.ON_HOLD;
        touch();
    }

    /**
     * 보류된 정산을 다시 지급 가능한 상태로 전환합니다.
     */
    public void releaseHold() {
        if (status != SettlementStatus.ON_HOLD) {
            throw new IllegalStateException(
                    "ON_HOLD 상태의 정산만 보류 해제할 수 있습니다."
            );
        }

        this.status = SettlementStatus.READY;
        touch();
    }

    private void touch() {
        this.updatedAt = OffsetDateTime.now();
    }

    private static BigDecimal normalizeMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.UNNECESSARY);
    }

    private static void validate(
            Long sellerId,
            LocalDate periodStart,
            LocalDate periodEnd,
            BigDecimal grossSalesAmount,
            BigDecimal commissionAmount,
            BigDecimal adjustmentAmount,
            Integer targetCount
    ) {
        validatePositiveId(sellerId, "sellerId");

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

        if (grossSalesAmount == null
                || grossSalesAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "grossSalesAmount는 0 이상이어야 합니다."
            );
        }

        if (commissionAmount == null
                || commissionAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "commissionAmount는 0 이상이어야 합니다."
            );
        }

        Objects.requireNonNull(
                adjustmentAmount,
                "adjustmentAmount는 필수입니다."
        );

        if (targetCount == null || targetCount <= 0) {
            throw new IllegalArgumentException(
                    "targetCount는 0보다 커야 합니다."
            );
        }
    }

    private static void validatePositiveId(
            Long value,
            String fieldName
    ) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(
                    fieldName + "는 0보다 커야 합니다."
            );
        }
    }
}