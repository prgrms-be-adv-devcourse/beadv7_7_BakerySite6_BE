package com.openbake.payment.presentation.dto;

import java.math.BigDecimal;

// 예치금 조회 응답
public record DepositResponse(
        Long memberId,
        BigDecimal balance,                // 현재 예치금 잔액
        boolean hasChargeInProgress        // READY/IN_PROGRESS 충전 존재 여부
) {
}
