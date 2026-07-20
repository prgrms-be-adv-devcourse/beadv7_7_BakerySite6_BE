package com.openbake.payment.presentation.dto;

import java.math.BigDecimal;

// 충전 요청 생성 응답 — 프론트가 이 정보로 토스페이먼츠 결제창을 띄운다
public record ChargeCreateResponse(
        Long chargeRequestId,
        String pgOrderId,    // PG에 보낼 주문번호 (UUID)
        BigDecimal amount
) {
}
