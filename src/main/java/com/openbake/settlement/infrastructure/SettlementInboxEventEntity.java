package com.openbake.settlement.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 정산 서비스가 수신한 이벤트 처리 이력입니다.
 *
 * 동일 eventId가 다시 전달되더라도 한 번만 처리하기 위해 사용합니다.
 */
@Getter
@Entity
@Table(
        name = "settlement_inbox_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlement_inbox_event_id",
                        columnNames = "event_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementInboxEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 이벤트를 구분하는 고유 ID입니다.
     *
     * event_id에는 유니크 제약조건이 있으므로
     * 동일한 이벤트를 두 번 저장할 수 없습니다.
     */
    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;
    /**
     * 처리한 이벤트의 종류입니다.
     *
     * 예: PURCHASE_CONFIRMED
     */
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

    private SettlementInboxEventEntity(
            String eventId,
            String eventType
    ) {
        validate(eventId, eventType);

        this.eventId = eventId.trim();
        this.eventType = eventType.trim();
        this.receivedAt = OffsetDateTime.now();
    }
    /**
     * 처리한 이벤트를 저장하기 위한 Inbox 엔티티를 생성합니다.
     */
    public static SettlementInboxEventEntity create(
            String eventId,
            String eventType
    ) {
        return new SettlementInboxEventEntity(eventId, eventType);
    }

    private static void validate(
            String eventId,
            String eventType
    ) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException(
                    "eventId는 필수입니다."
            );
        }

        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException(
                    "eventType은 필수입니다."
            );
        }
    }
}