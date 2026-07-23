package com.openbake.settlement.infrastructure;

import com.openbake.settlement.domain.Settlement;
import com.openbake.settlement.domain.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SettlementRepositoryAdapter
        implements SettlementRepository {

    private final SettlementJpaRepository settlementJpaRepository;

    @Override
    public Settlement save(Settlement settlement) {
        return settlementJpaRepository.save(settlement);
    }

    @Override
    public Optional<Settlement> findById(Long id) {
        return settlementJpaRepository.findById(id);
    }

    @Override
    public Optional<Settlement> findBySellerIdAndPeriodStartAndPeriodEnd(
            Long sellerId,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        return settlementJpaRepository
                .findBySellerIdAndPeriodStartAndPeriodEnd(
                        sellerId,
                        periodStart,
                        periodEnd
                );
    }

    @Override
    public boolean existsBySellerIdAndPeriodStartAndPeriodEnd(
            Long sellerId,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        return settlementJpaRepository
                .existsBySellerIdAndPeriodStartAndPeriodEnd(
                        sellerId,
                        periodStart,
                        periodEnd
                );
    }

    @Override
    public List<Settlement> findAllBySellerId(
            Long sellerId
    ) {
        return settlementJpaRepository
                .findAllBySellerIdOrderByPeriodStartDescIdDesc(
                        sellerId
                );
    }

    @Override
    public Optional<Settlement> findByIdAndSellerId(
            Long settlementId,
            Long sellerId
    ) {
        return settlementJpaRepository.findByIdAndSellerId(
                settlementId,
                sellerId
        );
    }
}