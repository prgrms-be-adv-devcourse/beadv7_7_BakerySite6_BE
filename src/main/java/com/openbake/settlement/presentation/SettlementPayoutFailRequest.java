package com.openbake.settlement.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SettlementPayoutFailRequest(

        @NotBlank(message = "지급 실패 사유는 필수입니다.")
        @Size(
                max = 500,
                message = "지급 실패 사유는 500자 이하여야 합니다."
        )
        String failureReason
) {
}