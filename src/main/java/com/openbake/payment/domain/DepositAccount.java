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
@Table(name = "deposit_accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepositAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(unique = true)
    private Long memberId;

    private BigDecimal balance;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static DepositAccount createMemberAccount(Long memberId) {
        DepositAccount account = new DepositAccount();
        account.accountType = AccountType.MEMBER;
        account.memberId = memberId;
        account.balance = BigDecimal.ZERO;
        account.createdAt = LocalDateTime.now();
        return account;
    }

    public static DepositAccount createPlatformAccount() {
        DepositAccount account = new DepositAccount();
        account.accountType = AccountType.PLATFORM;
        account.memberId = null;
        account.balance = null;
        account.createdAt = LocalDateTime.now();
        return account;
    }

    public void charge(BigDecimal amount) {
        validateMemberAccount();
        validatePositiveAmount(amount);
        this.balance = this.balance.add(amount);
    }

    public void deduct(BigDecimal amount) {
        validateMemberAccount();
        validatePositiveAmount(amount);
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("예치금 잔액이 부족합니다.");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void refund(BigDecimal amount) {
        validateMemberAccount();
        validatePositiveAmount(amount);
        this.balance = this.balance.add(amount);
    }

    private void validateMemberAccount() {
        if (this.accountType != AccountType.MEMBER) {
            throw new IllegalStateException("MEMBER 계정만 잔액 변경이 가능합니다.");
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
        }
    }
}
