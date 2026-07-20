package com.openbake.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "charge_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChargeRequest {

    private static final int EXPIRY_MINUTES = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PgProvider pgProvider;

    @Column(nullable = false, unique = true)
    private String pgOrderId;

    @Column(unique = true)
    private String pgPaymentKey;

    private String pgMethod;

    private String failureCode;

    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public static ChargeRequest create(Long memberId, BigDecimal amount, String pgOrderId) {
        ChargeRequest request = new ChargeRequest();
        request.memberId = memberId;
        request.amount = amount;
        request.status = ChargeStatus.READY;
        request.pgProvider = PgProvider.TOSS;
        request.pgOrderId = pgOrderId;
        request.requestedAt = LocalDateTime.now();
        request.expiresAt = request.requestedAt.plusMinutes(EXPIRY_MINUTES);
        return request;
    }

    public void markInProgress(String pgPaymentKey) {
        validateStatus(ChargeStatus.READY);
        validateNotExpired();
        this.status = ChargeStatus.IN_PROGRESS;
        this.pgPaymentKey = pgPaymentKey;
    }

    public void markDone(String pgMethod) {
        if (this.status == ChargeStatus.DONE) {
            return; // 멱등성: 이미 완료된 건은 무시
        }
        validateStatus(ChargeStatus.IN_PROGRESS);
        this.status = ChargeStatus.DONE;
        this.pgMethod = pgMethod;
        this.approvedAt = LocalDateTime.now();
    }

    public void markFailed(String failureCode, String failureReason) {
        validateStatus(ChargeStatus.IN_PROGRESS);
        this.status = ChargeStatus.FAILED;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
    }

    public void markExpired() {
        if (this.status != ChargeStatus.READY) {
            return;
        }
        this.status = ChargeStatus.EXPIRED;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isDone() {
        return this.status == ChargeStatus.DONE;
    }

    public void validateOwner(Long memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new IllegalStateException("본인의 충전 요청이 아닙니다.");
        }
    }

    public void validateAmountMatches(BigDecimal amount) {
        if (this.amount.compareTo(amount) != 0) {
            throw new IllegalArgumentException("충전 금액이 일치하지 않습니다.");
        }
    }

    private void validateStatus(ChargeStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException("승인할 수 없는 상태입니다: " + this.status);
        }
    }

    private void validateNotExpired() {
        if (isExpired()) {
            throw new IllegalStateException("만료된 충전 요청입니다.");
        }
    }
}
