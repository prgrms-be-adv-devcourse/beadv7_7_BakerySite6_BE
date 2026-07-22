package com.openbake.payment.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 충전 승인 완료 응답
public record ChargeApproveResponse(
        Long chargeRequestId,
        String status,              // "DONE" 고정
        BigDecimal chargedAmount,   // 충전된 금액
        BigDecimal balanceAfter,    // 충전 후 잔액
        String method,              // "CARD" / "EASY_PAY"
        LocalDateTime approvedAt    // PG 승인 시각
) {
}
