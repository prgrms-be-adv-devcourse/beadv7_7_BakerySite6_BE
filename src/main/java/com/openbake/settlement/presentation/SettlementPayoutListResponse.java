package com.openbake.settlement.presentation;

import com.openbake.settlement.application.SettlementPayoutResult;

import java.util.List;

public record SettlementPayoutListResponse(
        List<SettlementPayoutResponse> payouts
) {

    public static SettlementPayoutListResponse from(
            List<SettlementPayoutResult> results
    ) {
        return new SettlementPayoutListResponse(
                results.stream()
                        .map(SettlementPayoutResponse::from)
                        .toList()
        );
    }
}