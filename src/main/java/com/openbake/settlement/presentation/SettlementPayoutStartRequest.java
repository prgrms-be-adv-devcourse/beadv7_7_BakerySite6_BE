package com.openbake.settlement.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SettlementPayoutStartRequest(

        @NotBlank(message = "멱등키는 필수입니다.")
        @Size(
                max = 100,
                message = "멱등키는 100자 이하여야 합니다."
        )
        String idempotencyKey
) {
}