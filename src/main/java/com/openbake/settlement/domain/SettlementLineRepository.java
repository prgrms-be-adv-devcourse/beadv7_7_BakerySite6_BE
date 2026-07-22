package com.openbake.settlement.domain;

import java.util.List;
import java.util.Optional;

public interface SettlementLineRepository {

    SettlementLine save(SettlementLine settlementLine);

    /** 배치에서는 여러 Line을 한 번에 저장 */
    List<SettlementLine> saveAll(
            List<SettlementLine> settlementLines
    );

    Optional<SettlementLine> findByTargetId(Long targetId);

    List<SettlementLine> findAllBySettlementId(
            Long settlementId
    );

    boolean existsByTargetId(Long targetId);
}
