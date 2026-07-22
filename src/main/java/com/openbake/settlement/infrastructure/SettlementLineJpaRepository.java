package com.openbake.settlement.infrastructure;

import com.openbake.settlement.domain.SettlementLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementLineJpaRepository
        extends JpaRepository<SettlementLine, Long> {

    Optional<SettlementLine> findByTargetId(Long targetId);

    List<SettlementLine> findAllBySettlementIdOrderByIdAsc(
            Long settlementId
    );

    boolean existsByTargetId(Long targetId);
}