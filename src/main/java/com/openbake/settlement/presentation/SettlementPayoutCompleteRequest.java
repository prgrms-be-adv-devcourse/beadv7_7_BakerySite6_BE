package com.openbake.settlement.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SettlementPayoutCompleteRequest(

        @NotBlank(message = "외부 거래 ID는 필수입니다.")
        @Size(
                max = 100,
                message = "외부 거래 ID는 100자 이하여야 합니다."
        )
        String externalTransactionId
) {
}