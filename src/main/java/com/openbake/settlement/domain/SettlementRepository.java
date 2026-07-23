package com.openbake.settlement.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository {

    Settlement save(Settlement settlement);

    Optional<Settlement> findById(Long id);

    Optional<Settlement> findBySellerIdAndPeriodStartAndPeriodEnd(
            Long sellerId,
            LocalDate periodStart,
            LocalDate periodEnd
    );
    /** 중복 조회는 Spring Batch를 재실행했을 때
     * 같은 판매자와 기간의 정산서가
     * 다시 생성되는 것을 방지하는 데 사용
     **/
    boolean existsBySellerIdAndPeriodStartAndPeriodEnd(
            Long sellerId,
            LocalDate periodStart,
            LocalDate periodEnd
    );

    List<Settlement> findAllBySellerId(
            Long sellerId
    );

    Optional<Settlement> findByIdAndSellerId(
            Long settlementId,
            Long sellerId
    );
}