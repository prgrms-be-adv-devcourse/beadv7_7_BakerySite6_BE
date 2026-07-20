package com.openbake.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposit_account_id", nullable = false)
    private DepositAccount depositAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private BigDecimal amount;

    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferenceType referenceType;

    @Column(nullable = false)
    private Long referenceId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static WalletTransaction create(DepositAccount account,
                                           TransactionType transactionType,
                                           BigDecimal amount,
                                           BigDecimal balanceAfter,
                                           ReferenceType referenceType,
                                           Long referenceId) {
        WalletTransaction tx = new WalletTransaction();
        tx.depositAccount = account;
        tx.transactionType = transactionType;
        tx.amount = amount;
        tx.balanceAfter = balanceAfter;
        tx.referenceType = referenceType;
        tx.referenceId = referenceId;
        tx.createdAt = LocalDateTime.now();
        return tx;
    }
}
