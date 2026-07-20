package com.openbake.payment.presentation.dto;

import java.math.BigDecimal;

// 충전 승인 완료 응답
public record ChargeApproveResponse(
        Long chargeRequestId,
        BigDecimal chargedAmount,   // 충전된 금액
        BigDecimal balanceAfter     // 충전 후 잔액
) {
}
