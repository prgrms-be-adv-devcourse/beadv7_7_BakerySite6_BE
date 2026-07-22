package com.openbake.settlement.infrastructure;

import com.openbake.settlement.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SettlementJpaRepository
        extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findBySellerIdAndPeriodStartAndPeriodEnd(
            Long sellerId,
            LocalDate periodStart,
            LocalDate periodEnd
    );

    boolean existsBySellerIdAndPeriodStartAndPeriodEnd(
            Long sellerId,
            LocalDate periodStart,
            LocalDate periodEnd
    );
}