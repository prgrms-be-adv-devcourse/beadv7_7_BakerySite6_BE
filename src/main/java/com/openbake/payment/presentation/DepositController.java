package com.openbake.payment.presentation;

import com.openbake.common.response.ApiResponse;
import com.openbake.payment.application.DepositService;
import com.openbake.payment.domain.TransactionType;
import com.openbake.payment.presentation.dto.DepositResponse;
import com.openbake.payment.presentation.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deposit")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    /**
     * 예치금 잔액 조회.
     * 인증 연동 전이므로 memberId를 PathVariable로 받는다.
     * → 인증 붙으면 @AuthenticationPrincipal에서 memberId를 꺼내도록 교체 예정.
     */
    @GetMapping("/account/{memberId}")
    public ResponseEntity<ApiResponse<DepositResponse>> getBalance(@PathVariable Long memberId) {
        DepositResponse response = depositService.getBalance(memberId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 거래 내역 조회 (5-2).
     * 인증 연동 전이므로 memberId를 PathVariable로 받는다.
     */
    @GetMapping("/transactions/{memberId}")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @PathVariable Long memberId,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TransactionResponse> response = depositService.getTransactions(memberId, transactionType, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
