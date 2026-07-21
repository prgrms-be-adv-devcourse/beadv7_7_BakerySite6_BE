package com.openbake.settlement.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 정산 이벤트 Inbox 엔티티를 저장하고 조회하는
 * Spring Data JPA Repository입니다.
 *
 * 실제 구현체는 Spring Data JPA가 실행 시점에 자동으로 생성합니다.
 */
public interface SettlementInboxEventJpaRepository
        extends JpaRepository<SettlementInboxEventEntity, Long> {

    /**
     * 해당 이벤트 ID가 Inbox에 이미 저장되어 있는지 확인합니다.
     *
     * @param eventId 이벤트 고유 ID
     * @return 이미 저장된 이벤트라면 true
     */
    boolean existsByEventId(String eventId);
}