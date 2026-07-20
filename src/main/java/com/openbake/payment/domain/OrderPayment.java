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
@Table(name = "order_payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime refundedAt;

    public static OrderPayment create(Long orderId, Long memberId, BigDecimal amount) {
        OrderPayment payment = new OrderPayment();
        payment.orderId = orderId;
        payment.memberId = memberId;
        payment.amount = amount;
        payment.status = PaymentStatus.PAID;
        payment.paidAt = LocalDateTime.now();
        return payment;
    }

    public void confirm() {
        validateStatus(PaymentStatus.PAID);
        this.status = PaymentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void refund() {
        validateStatus(PaymentStatus.PAID);
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }

    public boolean isRefundable() {
        return this.status == PaymentStatus.PAID;
    }

    private void validateStatus(PaymentStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException("처리할 수 없는 결제 상태입니다: " + this.status);
        }
    }
}
