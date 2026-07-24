package com.openbake.settlement.presentation;

import com.openbake.common.response.ApiResponse;
import com.openbake.settlement.application.SettlementPaymentResult;
import com.openbake.settlement.application.SettlementPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/settlements")
@RequiredArgsConstructor
public class AdminSettlementPaymentController {

    private final SettlementPaymentService
            settlementPaymentService;

    @PostMapping("/{settlementId}/payments/start")
    public ApiResponse<SettlementPaymentResponse>
    startPayment(
            @PathVariable Long settlementId
    ) {
        SettlementPaymentResult result =
                settlementPaymentService.start(
                        settlementId
                );

        return ApiResponse.ok(
                SettlementPaymentResponse.from(result)
        );
    }

    @PostMapping("/{settlementId}/payments/complete")
    public ApiResponse<SettlementPaymentResponse>
    completePayment(
            @PathVariable Long settlementId
    ) {
        SettlementPaymentResult result =
                settlementPaymentService.complete(
                        settlementId
                );

        return ApiResponse.ok(
                SettlementPaymentResponse.from(result)
        );
    }

    @PostMapping("/{settlementId}/payments/fail")
    public ApiResponse<SettlementPaymentResponse>
    failPayment(
            @PathVariable Long settlementId
    ) {
        SettlementPaymentResult result =
                settlementPaymentService.fail(
                        settlementId
                );

        return ApiResponse.ok(
                SettlementPaymentResponse.from(result)
        );
    }
}