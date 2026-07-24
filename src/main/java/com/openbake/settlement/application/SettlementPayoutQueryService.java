package com.openbake.settlement.application;

import com.openbake.common.exception.EntityNotFoundException;
import com.openbake.settlement.domain.SettlementPayout;
import com.openbake.settlement.domain.SettlementPayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementPayoutQueryService {

    private final SettlementPayoutRepository payoutRepository;

    public SettlementPayoutResult getPayout(
            Long payoutId
    ) {
        validatePayoutId(payoutId);

        SettlementPayout payout =
                payoutRepository.findById(payoutId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "지급 이력을 찾을 수 없습니다. "
                                                + "payoutId="
                                                + payoutId
                                )
                        );

        return SettlementPayoutResult.from(payout);
    }

    public List<SettlementPayoutResult> getPayouts(
            Long settlementId
    ) {
        validateSettlementId(settlementId);

        return payoutRepository
                .findAllBySettlementId(settlementId)
                .stream()
                .map(SettlementPayoutResult::from)
                .toList();
    }

    private void validatePayoutId(Long payoutId) {
        if (payoutId == null || payoutId <= 0) {
            throw new IllegalArgumentException(
                    "payoutId는 0보다 커야 합니다."
            );
        }
    }

    private void validateSettlementId(Long settlementId) {
        if (settlementId == null || settlementId <= 0) {
            throw new IllegalArgumentException(
                    "settlementId는 0보다 커야 합니다."
            );
        }
    }
}