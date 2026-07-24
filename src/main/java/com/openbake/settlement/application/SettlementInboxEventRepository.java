package com.openbake.settlement.application;

/**
 * 정산 이벤트 Inbox 저장소입니다.
 *
 * 같은 이벤트가 여러 번 전달되더라도
 * 정산 대상이 중복 생성되지 않도록 처리 이력을 관리합니다.
 *
 * 실제 JPA 구현은 infrastructure 계층에 위치합니다.
 * Inbox는 정산 금액이나 정산 상태를 표현하는 핵심 도메인 객체가 아니라,
 * 이벤트 중복 처리를 위한 기술적 장치
 * SettlementInboxEventRepository
 * → 애플리케이션 유스케이스의 중복 처리 지원
 * → application 계층의 포트
 */
public interface SettlementInboxEventRepository {
    /**
     * 해당 이벤트가 이미 처리됐는지 확인합니다.
     *
     * @param eventId 이벤트 고유 ID
     * @return 이미 처리한 이벤트라면 true
     */
    boolean existsByEventId(String eventId);

    /**
     * 처리한 이벤트를 Inbox에 저장합니다.
     *
     * @param eventId 이벤트 고유 ID
     * @param eventType 이벤트 종류
     */
    void save(String eventId, String eventType);
}