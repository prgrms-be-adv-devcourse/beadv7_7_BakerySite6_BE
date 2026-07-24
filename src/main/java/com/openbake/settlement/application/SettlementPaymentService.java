package com.openbake.settlement.application;

import com.openbake.common.exception.EntityNotFoundException;
import com.openbake.settlement.domain.Settlement;
import com.openbake.settlement.domain.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementPaymentService {

    private final SettlementRepository settlementRepository;

    public SettlementPaymentResult start(
            Long settlementId
    ) {
        Settlement settlement = findSettlement(settlementId);

        settlement.startPaying();

        return SettlementPaymentResult.from(settlement);
    }

    public SettlementPaymentResult complete(
            Long settlementId
    ) {
        Settlement settlement = findSettlement(settlementId);

        settlement.complete();

        return SettlementPaymentResult.from(settlement);
    }

    public SettlementPaymentResult fail(
            Long settlementId
    ) {
        Settlement settlement = findSettlement(settlementId);

        settlement.failPayment();

        return SettlementPaymentResult.from(settlement);
    }

    private Settlement findSettlement(
            Long settlementId
    ) {
        validateSettlementId(settlementId);

        return settlementRepository.findById(settlementId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "정산 정보를 찾을 수 없습니다. "
                                        + "settlementId="
                                        + settlementId
                        )
                );
    }

    private void validateSettlementId(
            Long settlementId
    ) {
        if (settlementId == null || settlementId <= 0) {
            throw new IllegalArgumentException(
                    "settlementId는 0보다 커야 합니다."
            );
        }
    }
}