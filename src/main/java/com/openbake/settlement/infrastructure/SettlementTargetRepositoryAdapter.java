package com.openbake.settlement.infrastructure;

import com.openbake.settlement.domain.SettlementTarget;
import com.openbake.settlement.domain.SettlementTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 정산 대상 저장소의 JPA 구현체입니다.
 *
 * 애플리케이션과 도메인 계층은
 * SettlementTargetRepository 인터페이스에만 의존하고,
 * 실제 PostgreSQL 저장은 이 어댑터가 담당합니다.
 */
@Repository
@RequiredArgsConstructor
public class SettlementTargetRepositoryAdapter
        implements SettlementTargetRepository {

    private final SettlementTargetJpaRepository jpaRepository;

    /**
     * 정산 대상을 PostgreSQL에 저장합니다.
     */
    @Override
    public SettlementTarget save(SettlementTarget target) {
        return jpaRepository.save(target);
    }

    /**
     * 주문 ID와 주문 상품 ID로 정산 대상을 조회합니다.
     */
    @Override
    public Optional<SettlementTarget> findByOrderIdAndOrderItemId(
            Long orderId,
            Long orderItemId
    ) {
        return jpaRepository.findByOrderIdAndOrderItemId(
                orderId,
                orderItemId
        );
    }

    /**
     * 동일한 주문 상품에 대한 정산 대상이 존재하는지 확인합니다.
     */
    @Override
    public boolean existsByOrderIdAndOrderItemId(
            Long orderId,
            Long orderItemId
    ) {
        return jpaRepository.existsByOrderIdAndOrderItemId(
                orderId,
                orderItemId
        );
    }
}