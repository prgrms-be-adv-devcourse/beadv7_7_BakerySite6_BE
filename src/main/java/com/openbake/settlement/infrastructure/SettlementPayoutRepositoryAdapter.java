package com.openbake.settlement.infrastructure;

import com.openbake.settlement.domain.SettlementPayout;
import com.openbake.settlement.domain.SettlementPayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SettlementPayoutRepositoryAdapter
        implements SettlementPayoutRepository {

    private final SettlementPayoutJpaRepository
            settlementPayoutJpaRepository;

    @Override
    public SettlementPayout save(
            SettlementPayout payout
    ) {
        return settlementPayoutJpaRepository.save(payout);
    }

    @Override
    public Optional<SettlementPayout> findById(
            Long id
    ) {
        return settlementPayoutJpaRepository.findById(id);
    }

    @Override
    public Optional<SettlementPayout> findByIdempotencyKey(
            String idempotencyKey
    ) {
        return settlementPayoutJpaRepository
                .findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public List<SettlementPayout> findAllBySettlementId(
            Long settlementId
    ) {
        return settlementPayoutJpaRepository
                .findAllBySettlementIdOrderByRequestedAtDescIdDesc(
                        settlementId
                );
    }
}