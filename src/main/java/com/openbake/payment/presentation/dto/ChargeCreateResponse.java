package com.openbake.payment.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 충전 요청 생성 응답 — 프론트가 이 정보로 토스페이먼츠 결제창을 띄운다
public record ChargeCreateResponse(
        Long chargeRequestId,
        String pgOrderId,           // PG에 보낼 주문번호 (UUID)
        BigDecimal amount,
        String orderName,           // 결제창에 표시될 주문명
        LocalDateTime expiresAt     // 충전 요청 만료 시각 (요청 + 30분)
) {
}
