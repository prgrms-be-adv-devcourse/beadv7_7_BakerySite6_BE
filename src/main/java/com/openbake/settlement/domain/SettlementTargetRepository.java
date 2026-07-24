package com.openbake.settlement.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 정산 대상 저장소입니다.
 *
 * 실제 PostgreSQL/JPA 구현은 infrastructure 계층에서 담당합니다.
 */
public interface SettlementTargetRepository {

    /**
     * 정산 대상을 저장합니다.
     *
     * @param target 저장할 정산 대상
     * @return 저장된 정산 대상
     */
    SettlementTarget save(SettlementTarget target);

    List<SettlementTarget> saveAll(
            List<SettlementTarget> targets
    );

    /**
     * 주문 ID와 주문 상품 ID로 정산 대상을 조회합니다.
     *
     * 동일한 주문 상품이 중복으로 정산 대상에 등록되는 것을
     * 방지할 때 사용합니다.
     */
    Optional<SettlementTarget> findByOrderIdAndOrderItemId(
            Long orderId,
            Long orderItemId
    );

    /**
     * 주문 ID와 주문 상품 ID에 해당하는 정산 대상이 존재하는지 확인합니다.
     */
    boolean existsByOrderIdAndOrderItemId(
            Long orderId,
            Long orderItemId
    );
    /** 월 정산 배치에서는 특정 기간의 PENDING 대상을 판매자별로 조회 */
    List<SettlementTarget> findAllPendingTargets(
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd
    );
}