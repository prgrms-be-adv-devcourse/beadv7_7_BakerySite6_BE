package com.openbake.payment.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 거래 내역 조회 응답 (5-2).
 * WalletTransaction 1건을 프론트에 내려주는 DTO.
 */
public record TransactionResponse(
        Long id,
        String transactionType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        String referenceType,
        Long referenceId,
        LocalDateTime createdAt
) {
}
