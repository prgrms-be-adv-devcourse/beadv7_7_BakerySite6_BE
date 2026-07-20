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

/**
 * 충전 요청.
 * TossPayments를 통한 예치금 충전 과정을 관리한다.
 * 외부 PG 호출이 포함되므로 트랜잭션을 분리해서 처리한다. (ARD-002 참고)
 *
 * 상태 전이: READY → IN_PROGRESS → DONE (성공)
 *                                → FAILED (실패)
 *           READY → EXPIRED (30분 초과)
 */
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
    private BigDecimal amount;  // 충전 요청 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PgProvider pgProvider;  // 현재는 TOSS만 사용

    @Column(nullable = false, unique = true)
    private String pgOrderId;  // PG에 보내는 주문번호 (UUID 문자열)

    @Column(unique = true)
    private String pgPaymentKey;  // PG가 돌려주는 결제 키 (승인 요청 시 받음)

    private String pgMethod;  // 결제 수단 (카드, 계좌이체 등) — PG 승인 후 받음

    private String failureCode;    // PG 실패 코드
    private String failureReason;  // PG 실패 사유

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;  // PG 승인 완료 시각

    @Column(nullable = false)
    private LocalDateTime expiresAt;  // 요청 후 30분 뒤 만료

    // 충전 요청 생성 — READY 상태, 30분 뒤 만료
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

    // PG에 승인 요청 보낼 때 — READY → IN_PROGRESS
    public void markInProgress(String pgPaymentKey) {
        validateStatus(ChargeStatus.READY);
        validateNotExpired();
        this.status = ChargeStatus.IN_PROGRESS;
        this.pgPaymentKey = pgPaymentKey;
    }

    // PG 승인 성공 — IN_PROGRESS → DONE (이미 DONE이면 무시: 멱등성)
    public void markDone(String pgMethod) {
        if (this.status == ChargeStatus.DONE) {
            return;
        }
        validateStatus(ChargeStatus.IN_PROGRESS);
        this.status = ChargeStatus.DONE;
        this.pgMethod = pgMethod;
        this.approvedAt = LocalDateTime.now();
    }

    // PG 승인 실패 — IN_PROGRESS → FAILED
    public void markFailed(String failureCode, String failureReason) {
        validateStatus(ChargeStatus.IN_PROGRESS);
        this.status = ChargeStatus.FAILED;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
    }

    // 배치에서 30분 초과된 요청을 만료 처리 — READY일 때만
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

    // 본인 충전 요청인지 확인
    public void validateOwner(Long memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new IllegalStateException("본인의 충전 요청이 아닙니다.");
        }
    }

    // PG에서 돌아온 금액이 요청 금액과 일치하는지 확인 (위변조 방지)
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
