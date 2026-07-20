package com.openbake.payment.presentation.dto;

import java.math.BigDecimal;

// 충전 요청 생성 — 클라이언트가 "얼마 충전할래요" 보내는 요청
public record ChargeCreateRequest(
        Long memberId,   // 인증 연동 전이므로 직접 받음
        BigDecimal amount // 충전 금액
) {
}
