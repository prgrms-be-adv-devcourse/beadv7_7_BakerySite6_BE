package com.openbake.settlement.presentation;

import com.openbake.common.response.ApiResponse;
import com.openbake.settlement.application.SettlementPayoutQueryService;
import com.openbake.settlement.application.SettlementPayoutResult;
import com.openbake.settlement.application.SettlementPayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/v1")
@RequiredArgsConstructor
public class AdminSettlementPayoutController {

    private final SettlementPayoutService payoutService;
    private final SettlementPayoutQueryService payoutQueryService;

    @PostMapping("/settlements/{settlementId}/payouts")
    public ApiResponse<SettlementPayoutResponse> start(
            @PathVariable Long settlementId,
            @Valid
            @RequestBody SettlementPayoutStartRequest request
    ) {
        SettlementPayoutResult result =
                payoutService.start(
                        settlementId,
                        request.idempotencyKey()
                );

        return ApiResponse.ok(
                SettlementPayoutResponse.from(result)
        );
    }

    @PostMapping("/settlement-payouts/{payoutId}/complete")
    public ApiResponse<SettlementPayoutResponse> complete(
            @PathVariable Long payoutId,
            @Valid
            @RequestBody SettlementPayoutCompleteRequest request
    ) {
        SettlementPayoutResult result =
                payoutService.complete(
                        payoutId,
                        request.externalTransactionId()
                );

        return ApiResponse.ok(
                SettlementPayoutResponse.from(result)
        );
    }

    @PostMapping("/settlement-payouts/{payoutId}/fail")
    public ApiResponse<SettlementPayoutResponse> fail(
            @PathVariable Long payoutId,
            @Valid
            @RequestBody SettlementPayoutFailRequest request
    ) {
        SettlementPayoutResult result =
                payoutService.fail(
                        payoutId,
                        request.failureReason()
                );

        return ApiResponse.ok(
                SettlementPayoutResponse.from(result)
        );
    }

    @GetMapping("/settlement-payouts/{payoutId}")
    public ApiResponse<SettlementPayoutResponse> getPayout(
            @PathVariable Long payoutId
    ) {
        SettlementPayoutResult result =
                payoutQueryService.getPayout(payoutId);

        return ApiResponse.ok(
                SettlementPayoutResponse.from(result)
        );
    }

    @GetMapping("/settlements/{settlementId}/payouts")
    public ApiResponse<SettlementPayoutListResponse> getPayouts(
            @PathVariable Long settlementId
    ) {
        List<SettlementPayoutResult> results =
                payoutQueryService.getPayouts(settlementId);

        return ApiResponse.ok(
                SettlementPayoutListResponse.from(results)
        );
    }
}