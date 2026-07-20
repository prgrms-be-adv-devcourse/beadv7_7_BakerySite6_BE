package com.openbake.payment.presentation;

import com.openbake.common.response.ApiResponse;
import com.openbake.payment.application.DepositService;
import com.openbake.payment.presentation.dto.DepositResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    /**
     * 예치금 잔액 조회.
     * 인증 연동 전이므로 memberId를 PathVariable로 받는다.
     * → 인증 붙으면 @AuthenticationPrincipal에서 memberId를 꺼내도록 교체 예정.
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<DepositResponse>> getBalance(@PathVariable Long memberId) {
        DepositResponse response = depositService.getBalance(memberId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
