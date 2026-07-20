package com.openbake.payment.domain;

// DepositAccount에서 사용하는 계좌 유형
public enum AccountType {
    MEMBER,    // 회원 예치금 계좌 (회원당 1개, 잔액 추적)
    PLATFORM   // 플랫폼 수익 계좌 (시스템에 1개, 거래 내역만 기록)
}
