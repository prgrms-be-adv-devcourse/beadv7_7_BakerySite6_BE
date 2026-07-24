package com.openbake.payment.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 충전 상태 조회 응답 (5-5).
 * PG_TIMEOUT(504) 이후 프론트가 폴링하거나, 충전 내역에서 상태를 확인할 때 사용.
 */
public record ChargeStatusResponse(
        Long chargeRequestId,
        BigDecimal amount,
        String status,
        String method,
        String failureCode,
        String failureReason,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime expiresAt
) {
}
