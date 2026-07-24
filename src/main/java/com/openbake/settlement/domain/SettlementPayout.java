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

@Getter
@Entity
@Table(
        name = "settlement_payouts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlement_payout_idempotency_key",
                        columnNames = "idempotency_key"
                )
        },
        indexes = {
                @Index(
                        name = "idx_settlement_payout_settlement_id",
                        columnList = "settlement_id"
                ),
                @Index(
                        name = "idx_settlement_payout_status",
                        columnList = "status"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "settlement_id", nullable = false)
    private Long settlementId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "payout_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal payoutAmount;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SettlementPayoutStatus status;

    @Column(name = "external_transaction_id", length = 100)
    private String externalTransactionId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "failed_at")
    private OffsetDateTime failedAt;

    public static SettlementPayout create(
            Long settlementId,
            Long sellerId,
            BigDecimal payoutAmount,
            String idempotencyKey
    ) {
        validateIdempotencyKey(idempotencyKey);

        SettlementPayout payout = new SettlementPayout();
        payout.settlementId = settlementId;
        payout.sellerId = sellerId;
        payout.payoutAmount = payoutAmount;
        payout.idempotencyKey = idempotencyKey;
        payout.status = SettlementPayoutStatus.REQUESTED;
        payout.requestedAt = OffsetDateTime.now();

        return payout;
    }

    public void startProcessing() {
        if (status != SettlementPayoutStatus.REQUESTED) {
            throw new IllegalStateException(
                    "처리 중 상태로 변경할 수 없는 지급 상태입니다. status=" + status
            );
        }

        status = SettlementPayoutStatus.PROCESSING;
    }

    public void complete(String externalTransactionId) {
        if (status != SettlementPayoutStatus.PROCESSING) {
            throw new IllegalStateException(
                    "완료 처리할 수 없는 지급 상태입니다. status=" + status
            );
        }

        if (externalTransactionId == null || externalTransactionId.isBlank()) {
            throw new IllegalArgumentException(
                    "외부 거래 ID는 필수입니다."
            );
        }

        status = SettlementPayoutStatus.COMPLETED;
        this.externalTransactionId = externalTransactionId;
        this.completedAt = OffsetDateTime.now();
        this.failureReason = null;
    }

    public void fail(String failureReason) {
        if (status != SettlementPayoutStatus.PROCESSING) {
            throw new IllegalStateException(
                    "실패 처리할 수 없는 지급 상태입니다. status=" + status
            );
        }

        if (failureReason == null || failureReason.isBlank()) {
            throw new IllegalArgumentException(
                    "지급 실패 사유는 필수입니다."
            );
        }

        status = SettlementPayoutStatus.FAILED;
        this.failureReason = failureReason;
        this.failedAt = OffsetDateTime.now();
    }

    private static void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "멱등키는 필수입니다."
            );
        }
    }
}