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
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 구매 확정된 주문 항목의 정산 대상입니다.
 *
 * 주문 상품 하나당 하나의 SettlementTarget이 생성되며,
 * 이후 월별 정산 작업에서 특정 Settlement에 배정됩니다.
 *
 * 코드설명: 상태 변경은 Setter가 아니라 assignTo()와 exclude()를 통해서만 가능하게 유지
 */
@Getter
@Entity
@Table(
        name = "settlement_targets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlement_target_order_item",
                        columnNames = {
                                "order_id",
                                "order_item_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_settlement_target_status_confirmed_at",
                        columnList = "status, confirmed_at"
                ),
                @Index(
                        name = "idx_settlement_target_seller_id",
                        columnList = "seller_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이 정산 대상이 포함된 정산의 ID입니다.
     *
     * 정산에 배정되기 전에는 null입니다.
     */
    @Column(name = "settlement_id")
    private Long settlementId;

    /**
     * 주문 ID입니다.
     */
    @Column(
            name = "order_id",
            nullable = false,
            updatable = false
    )
    private Long orderId;

    /**
     * 주문 상품 ID입니다.
     */
    @Column(
            name = "order_item_id",
            nullable = false,
            updatable = false
    )
    private Long orderItemId;

    /**
     * 상품 판매자 ID입니다.
     */
    @Column(
            name = "seller_id",
            nullable = false,
            updatable = false
    )
    private Long sellerId;

    /**
     * 구매 확정 당시의 상품명입니다.
     *
     * 이후 상품명이 변경되더라도 과거 정산 내역을 유지하기 위해
     * 구매 확정 시점의 값을 저장합니다.
     */
    @Column(
            name = "product_name",
            nullable = false,
            length = 200
    )
    private String productName;

    /**
     * 구매 수량입니다.
     */
    @Column(
            name = "quantity",
            nullable = false
    )
    private Integer quantity;

    /**
     * 수수료를 차감하기 전 총 판매 금액입니다.
     * @Column(precision):소수점을 포함한 숫자의 전체 자릿수
     * @Column(scale):소수점 아래 자리수 (기본값: 0)
     */
    @Column(
            name = "gross_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal grossAmount;

    /**
     * 구매 확정 시점에 적용된 수수료율입니다.
     *
     * 예:
     * 0.1000 = 10%
     */
    @Column(
            name = "commission_rate",
            nullable = false,
            precision = 7,
            scale = 4
    )
    private BigDecimal commissionRate;

    /**
     * 구매 확정 시각입니다.
     */
    @Column(
            name = "confirmed_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime confirmedAt;

    /**
     * 현재 정산 대상 처리 상태입니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private SettlementTargetStatus status;

    /**
     * 정산 대상 데이터가 생성된 시각입니다.
     */
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    private SettlementTarget(
            Long orderId,
            Long orderItemId,
            Long sellerId,
            String productName,
            Integer quantity,
            BigDecimal grossAmount,
            BigDecimal commissionRate,
            OffsetDateTime confirmedAt
    ) {
        validate(
                orderId,
                orderItemId,
                sellerId,
                productName,
                quantity,
                grossAmount,
                commissionRate,
                confirmedAt
        );

        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.sellerId = sellerId;
        this.productName = productName.trim();
        this.quantity = quantity;
        this.grossAmount = grossAmount;
        this.commissionRate = commissionRate;
        this.confirmedAt = confirmedAt;

        this.status = SettlementTargetStatus.PENDING;
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * 구매 확정 정보를 바탕으로 정산 대상을 생성합니다.
     */
    public static SettlementTarget create(
            Long orderId,
            Long orderItemId,
            Long sellerId,
            String productName,
            Integer quantity,
            BigDecimal grossAmount,
            BigDecimal commissionRate,
            OffsetDateTime confirmedAt
    ) {
        return new SettlementTarget(
                orderId,
                orderItemId,
                sellerId,
                productName,
                quantity,
                grossAmount,
                commissionRate,
                confirmedAt
        );
    }

    /**
     * 정산 대상을 특정 정산 건에 배정합니다.
     *
     * PENDING 상태인 정산 대상만 배정할 수 있습니다.
     */
    public void assignTo(Long settlementId) {
        Objects.requireNonNull(
                settlementId,
                "settlementId는 필수입니다."
        );

        if (status != SettlementTargetStatus.PENDING) {
            throw new IllegalStateException(
                    "PENDING 상태의 정산 대상만 정산에 배정할 수 있습니다."
            );
        }

        this.settlementId = settlementId;
        this.status = SettlementTargetStatus.ASSIGNED;
    }

    /**
     * 취소 또는 환불 등의 이유로 정산 대상에서 제외합니다.
     */
    public void exclude() {
        if (status == SettlementTargetStatus.ASSIGNED) {
            throw new IllegalStateException(
                    "이미 정산에 배정된 대상은 제외할 수 없습니다."
            );
        }

        this.status = SettlementTargetStatus.EXCLUDED;
    }

    /**
     * 수수료 금액을 계산합니다.
     */
    public BigDecimal calculateCommissionAmount() {
        return grossAmount
                .multiply(commissionRate)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 판매자에게 지급할 금액을 계산합니다.
     *
     * 지급 금액 = 총 판매 금액 - 수수료
     */
    public BigDecimal calculateNetAmount() {
        return grossAmount.subtract(calculateCommissionAmount());
    }

    private void validate(
            Long orderId,
            Long orderItemId,
            Long sellerId,
            String productName,
            Integer quantity,
            BigDecimal grossAmount,
            BigDecimal commissionRate,
            OffsetDateTime confirmedAt
    ) {
        Objects.requireNonNull(orderId, "orderId는 필수입니다.");
        Objects.requireNonNull(orderItemId, "orderItemId는 필수입니다.");
        Objects.requireNonNull(sellerId, "sellerId는 필수입니다.");
        Objects.requireNonNull(productName, "productName은 필수입니다.");
        Objects.requireNonNull(quantity, "quantity는 필수입니다.");
        Objects.requireNonNull(grossAmount, "grossAmount는 필수입니다.");
        Objects.requireNonNull(commissionRate, "commissionRate는 필수입니다.");
        Objects.requireNonNull(confirmedAt, "confirmedAt은 필수입니다.");

        if (orderId <= 0) {
            throw new IllegalArgumentException(
                    "orderId는 1 이상이어야 합니다."
            );
        }

        if (orderItemId <= 0) {
            throw new IllegalArgumentException(
                    "orderItemId는 1 이상이어야 합니다."
            );
        }

        if (sellerId <= 0) {
            throw new IllegalArgumentException(
                    "sellerId는 1 이상이어야 합니다."
            );
        }

        if (productName.isBlank()) {
            throw new IllegalArgumentException(
                    "productName은 비어 있을 수 없습니다."
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "quantity는 1 이상이어야 합니다."
            );
        }

        if (grossAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "grossAmount는 음수일 수 없습니다."
            );
        }

        if (commissionRate.signum() < 0
                || commissionRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "commissionRate는 0 이상 1 이하여야 합니다."
            );
        }
    }
}