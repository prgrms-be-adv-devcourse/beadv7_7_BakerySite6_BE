package com.openbake.settlement.infrastructure;

import com.openbake.settlement.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
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

    /**
     * 정산 목록은 최신 정산 기간부터 보이도록 정렬
     * periodStart DESC
     * id DESC
     * */
    List<Settlement> findAllBySellerIdOrderByPeriodStartDescIdDesc(
            Long sellerId
    );

    Optional<Settlement> findByIdAndSellerId(
            Long settlementId,
            Long sellerId
    );
}