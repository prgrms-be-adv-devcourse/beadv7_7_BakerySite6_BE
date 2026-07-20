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
import java.util.Objects;
import java.util.UUID;

/**
 * 정산 도메인에서 처리한 이벤트의 이력을 저장하는 Inbox 엔티티입니다.
 *
 * 동일한 eventId가 다시 전달되더라도 중복으로 처리되지 않도록 합니다.
 */
@Getter
@Entity
@Table(
        name = "settlement_inboxes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlement_inbox_event_id",
                        columnNames = "event_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementInboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이벤트를 구분하는 고유 ID입니다.
     *
     * event_id에는 유니크 제약조건이 있으므로
     * 동일한 이벤트를 두 번 저장할 수 없습니다.
     */
    @Column(
            name = "event_id",
            nullable = false,
            updatable = false
    )
    private UUID eventId;

    /**
     * 처리한 이벤트의 종류입니다.
     *
     * 예: PURCHASE_CONFIRMED
     */
    @Column(
            name = "event_type",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String eventType;

    /**
     * 이벤트를 Inbox에 저장한 시각입니다.
     */
    @Column(
            name = "received_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime receivedAt;

    private SettlementInboxEntity(
            UUID eventId,
            String eventType
    ) {
        validate(eventId, eventType);

        this.eventId = eventId;
        this.eventType = eventType.trim();
        this.receivedAt = OffsetDateTime.now();
    }

    /**
     * 처리한 이벤트를 저장하기 위한 Inbox 엔티티를 생성합니다.
     */
    public static SettlementInboxEntity create(
            UUID eventId,
            String eventType
    ) {
        return new SettlementInboxEntity(
                eventId,
                eventType
        );
    }

    private void validate(
            UUID eventId,
            String eventType
    ) {
        Objects.requireNonNull(
                eventId,
                "eventId는 필수입니다."
        );

        Objects.requireNonNull(
                eventType,
                "eventType은 필수입니다."
        );

        if (eventType.isBlank()) {
            throw new IllegalArgumentException(
                    "eventType은 비어 있을 수 없습니다."
            );
        }
    }
}