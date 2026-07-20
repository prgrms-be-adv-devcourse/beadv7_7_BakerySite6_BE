package com.openbake.settlement.infrastructure;

import com.openbake.settlement.domain.SettlementTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * SettlementTarget 엔티티를 저장하고 조회하는
 * Spring Data JPA 리포지토리입니다.
 *
 * 이 인터페이스는 infrastructure 계층에 위치하며,
 * 실제 구현체는 Spring Data JPA가 실행 시점에 자동으로 생성합니다.
 */
public interface SettlementTargetJpaRepository
        extends JpaRepository<SettlementTarget, Long> {

    /**
     * 주문 ID와 주문 상품 ID로 정산 대상을 조회합니다.
     */
    Optional<SettlementTarget> findByOrderIdAndOrderItemId(
            Long orderId,
            Long orderItemId
    );

    /**
     * 주문 ID와 주문 상품 ID에 해당하는 정산 대상의
     * 존재 여부를 확인합니다.
     */
    boolean existsByOrderIdAndOrderItemId(
            Long orderId,
            Long orderItemId
    );
}