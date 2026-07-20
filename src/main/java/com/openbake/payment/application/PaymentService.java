package com.openbake.payment.application;

import com.openbake.payment.domain.AccountType;
import com.openbake.payment.domain.DepositAccount;
import com.openbake.payment.domain.OrderPayment;
import com.openbake.payment.domain.ReferenceType;
import com.openbake.payment.domain.TransactionType;
import com.openbake.payment.domain.WalletTransaction;
import com.openbake.payment.infrastructure.DepositAccountRepository;
import com.openbake.payment.infrastructure.OrderPaymentRepository;
import com.openbake.payment.infrastructure.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final DepositAccountRepository depositAccountRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Transactional
    public void pay(Long orderId, Long memberId, BigDecimal amount) {
        DepositAccount memberAccount = getOrCreateMemberAccount(memberId);
        DepositAccount platformAccount = getPlatformAccount();

        memberAccount.deduct(amount);

        OrderPayment payment = OrderPayment.create(orderId, memberId, amount);
        orderPaymentRepository.save(payment);

        walletTransactionRepository.save(WalletTransaction.create(
                memberAccount,
                TransactionType.PAYMENT,
                amount.negate(),
                memberAccount.getBalance(),
                ReferenceType.ORDER_PAYMENT,
                payment.getId()
        ));

        walletTransactionRepository.save(WalletTransaction.create(
                platformAccount,
                TransactionType.PAYMENT,
                amount,
                null,
                ReferenceType.ORDER_PAYMENT,
                payment.getId()
        ));
    }

    @Transactional
    public void refund(Long orderId) {
        OrderPayment payment = orderPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다."));

        payment.refund();

        DepositAccount memberAccount = depositAccountRepository.findByMemberId(payment.getMemberId())
                .orElseThrow(() -> new IllegalStateException("예치금 계좌를 찾을 수 없습니다."));
        DepositAccount platformAccount = getPlatformAccount();

        memberAccount.refund(payment.getAmount());

        walletTransactionRepository.save(WalletTransaction.create(
                memberAccount,
                TransactionType.REFUND,
                payment.getAmount(),
                memberAccount.getBalance(),
                ReferenceType.ORDER_PAYMENT,
                payment.getId()
        ));

        walletTransactionRepository.save(WalletTransaction.create(
                platformAccount,
                TransactionType.REFUND,
                payment.getAmount().negate(),
                null,
                ReferenceType.ORDER_PAYMENT,
                payment.getId()
        ));
    }

    @Transactional
    public void confirmPayment(Long orderId) {
        OrderPayment payment = orderPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다."));

        payment.confirm();
    }

    private DepositAccount getOrCreateMemberAccount(Long memberId) {
        return depositAccountRepository.findByMemberId(memberId)
                .orElseGet(() -> depositAccountRepository.save(
                        DepositAccount.createMemberAccount(memberId)
                ));
    }

    private DepositAccount getPlatformAccount() {
        return depositAccountRepository.findAll().stream()
                .filter(a -> a.getAccountType() == AccountType.PLATFORM)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("PLATFORM 계정이 존재하지 않습니다."));
    }
}
