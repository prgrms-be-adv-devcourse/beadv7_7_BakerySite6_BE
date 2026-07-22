package com.openbake.settlement.infrastructure;

import com.openbake.settlement.domain.SettlementLine;
import com.openbake.settlement.domain.SettlementLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SettlementLineRepositoryAdapter
        implements SettlementLineRepository {

    private final SettlementLineJpaRepository
            settlementLineJpaRepository;

    @Override
    public SettlementLine save(
            SettlementLine settlementLine
    ) {
        return settlementLineJpaRepository.save(
                settlementLine
        );
    }

    @Override
    public List<SettlementLine> saveAll(
            List<SettlementLine> settlementLines
    ) {
        return settlementLineJpaRepository.saveAll(
                settlementLines
        );
    }

    @Override
    public Optional<SettlementLine> findByTargetId(
            Long targetId
    ) {
        return settlementLineJpaRepository.findByTargetId(
                targetId
        );
    }

    @Override
    public List<SettlementLine> findAllBySettlementId(
            Long settlementId
    ) {
        return settlementLineJpaRepository
                .findAllBySettlementIdOrderByIdAsc(
                        settlementId
                );
    }

    @Override
    public boolean existsByTargetId(Long targetId) {
        return settlementLineJpaRepository
                .existsByTargetId(targetId);
    }
}