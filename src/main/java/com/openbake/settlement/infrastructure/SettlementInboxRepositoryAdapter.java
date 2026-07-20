package com.openbake.settlement.infrastructure;

import com.openbake.settlement.application.SettlementInboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * 정산 이벤트 Inbox 저장소의 JPA 구현체입니다.
 *
 * 애플리케이션 계층은 SettlementInboxRepository 인터페이스에만 의존하고,
 * 실제 PostgreSQL 저장은 이 어댑터가 담당합니다.
 */
@Repository
@RequiredArgsConstructor
public class SettlementInboxRepositoryAdapter
        implements SettlementInboxRepository {

    private final SettlementInboxJpaRepository jpaRepository;

    /**
     * 해당 이벤트가 이미 처리되었는지 확인합니다.
     */
    @Override
    public boolean existsByEventId(UUID eventId) {
        return jpaRepository.existsByEventId(eventId);
    }

    /**
     * 처리한 이벤트를 Inbox 테이블에 저장합니다.
     */
    @Override
    public void save(
            UUID eventId,
            String eventType
    ) {
        SettlementInboxEntity entity =
                SettlementInboxEntity.create(
                        eventId,
                        eventType
                );

        jpaRepository.save(entity);
    }
}