package com.openbake.settlement.presentation;

public record SettlementTestResponse(
        String status,
        String message
) {

    public static SettlementTestResponse success(String message) {
        return new SettlementTestResponse(
                "OK",
                message
        );
    }
}