package com.openbake.settlement.domain;

import java.util.List;
import java.util.Optional;

public interface SettlementPayoutRepository {

    SettlementPayout save(SettlementPayout payout);

    Optional<SettlementPayout> findById(Long id);

    Optional<SettlementPayout> findByIdempotencyKey(
            String idempotencyKey
    );

    List<SettlementPayout> findAllBySettlementId(
            Long settlementId
    );
}