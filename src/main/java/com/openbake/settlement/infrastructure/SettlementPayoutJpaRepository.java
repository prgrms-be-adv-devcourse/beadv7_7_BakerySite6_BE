package com.openbake.settlement.infrastructure;

import com.openbake.settlement.domain.SettlementPayout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementPayoutJpaRepository
        extends JpaRepository<SettlementPayout, Long> {

    Optional<SettlementPayout> findByIdempotencyKey(
            String idempotencyKey
    );

    List<SettlementPayout>
    findAllBySettlementIdOrderByRequestedAtDescIdDesc(
            Long settlementId
    );
}