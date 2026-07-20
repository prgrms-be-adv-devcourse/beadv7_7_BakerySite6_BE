package com.openbake.settlement.application;

import com.openbake.settlement.domain.SettlementTarget;
import com.openbake.settlement.domain.SettlementTargetRepository;
import com.openbake.settlement.domain.SettlementTargetStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 구매확정 이벤트 처리 서비스 단위 테스트입니다.
 *
 * 실제 데이터베이스를 사용하지 않고 Repository를 Mock으로 대체합니다.
 * 1. 처음 들어온 이벤트
 *    → SettlementTarget 저장
 *    → Inbox 저장
 *    → duplicate=false
 *
 * 2. 같은 eventId 재전송
 *    → SettlementTarget 저장 안 함
 *    → Inbox 저장 안 함
 *    → duplicate=true
 *
 * 3. eventId는 다르지만 같은 주문 상품
 *    → SettlementTarget 저장 안 함
 *    → 새 eventId는 Inbox 저장
 *    → duplicate=true
 */
@ExtendWith(MockitoExtension.class)
class SettlementEventServiceTest {

    @Mock
    private SettlementInboxRepository settlementInboxRepository;

    @Mock
    private SettlementTargetRepository settlementTargetRepository;

    private SettlementEventService settlementEventService;

    @BeforeEach
    void setUp() {
        settlementEventService = new SettlementEventService(
                settlementInboxRepository,
                settlementTargetRepository
        );
    }

    @Test
    @DisplayName("새 구매확정 이벤트를 받으면 정산 대상과 Inbox를 저장한다")
    void receive_newEvent_createsSettlementTarget() {
        // given
        UUID eventId = UUID.randomUUID();

        ReceivePurchaseConfirmedCommand command =
                createCommand(
                        eventId,
                        1001L,
                        2001L
                );

        when(settlementInboxRepository.existsByEventId(eventId))
                .thenReturn(false);

        when(settlementTargetRepository.findByOrderIdAndOrderItemId(
                command.orderId(),
                command.orderItemId()
        )).thenReturn(Optional.empty());

        SettlementTarget savedTarget = mock(SettlementTarget.class);

        when(savedTarget.getId()).thenReturn(1L);

        when(settlementTargetRepository.save(
                any(SettlementTarget.class)
        )).thenReturn(savedTarget);

        // when
        SettlementEventResult result =
                settlementEventService.receive(command);

        // then
        assertAll(
                () -> assertEquals(eventId, result.eventId()),
                () -> assertEquals(1L, result.settlementTargetId()),
                () -> assertFalse(result.duplicate())
        );

        ArgumentCaptor<SettlementTarget> targetCaptor =
                ArgumentCaptor.forClass(SettlementTarget.class);

        verify(settlementTargetRepository)
                .save(targetCaptor.capture());

        SettlementTarget createdTarget =
                targetCaptor.getValue();

        assertAll(
                () -> assertEquals(
                        command.orderId(),
                        createdTarget.getOrderId()
                ),
                () -> assertEquals(
                        command.orderItemId(),
                        createdTarget.getOrderItemId()
                ),
                () -> assertEquals(
                        command.sellerId(),
                        createdTarget.getSellerId()
                ),
                () -> assertEquals(
                        command.productName(),
                        createdTarget.getProductName()
                ),
                () -> assertEquals(
                        command.quantity(),
                        createdTarget.getQuantity()
                ),
                () -> assertEquals(
                        command.grossAmount(),
                        createdTarget.getGrossAmount()
                ),
                () -> assertEquals(
                        new BigDecimal("0.1000"),
                        createdTarget.getCommissionRate()
                ),
                () -> assertEquals(
                        SettlementTargetStatus.PENDING,
                        createdTarget.getStatus()
                )
        );

        verify(settlementInboxRepository).save(
                eventId,
                "PURCHASE_CONFIRMED"
        );
    }

    @Test
    @DisplayName("같은 eventId가 이미 처리됐다면 정산 대상을 새로 저장하지 않는다")
    void receive_duplicatedEvent_doesNotCreateTarget() {
        // given
        UUID eventId = UUID.randomUUID();

        ReceivePurchaseConfirmedCommand command =
                createCommand(
                        eventId,
                        1001L,
                        2001L
                );

        SettlementTarget existingTarget =
                mock(SettlementTarget.class);

        when(existingTarget.getId()).thenReturn(1L);

        when(settlementInboxRepository.existsByEventId(eventId))
                .thenReturn(true);

        when(settlementTargetRepository.findByOrderIdAndOrderItemId(
                command.orderId(),
                command.orderItemId()
        )).thenReturn(Optional.of(existingTarget));

        // when
        SettlementEventResult result =
                settlementEventService.receive(command);

        // then
        assertAll(
                () -> assertEquals(eventId, result.eventId()),
                () -> assertEquals(1L, result.settlementTargetId()),
                () -> assertTrue(result.duplicate())
        );

        verify(settlementTargetRepository, never())
                .save(any(SettlementTarget.class));

        verify(settlementInboxRepository, never())
                .save(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("eventId는 다르지만 같은 주문 상품이 존재하면 Inbox만 저장한다")
    void receive_sameOrderItem_savesOnlyInbox() {
        // given
        UUID eventId = UUID.randomUUID();

        ReceivePurchaseConfirmedCommand command =
                createCommand(
                        eventId,
                        1001L,
                        2001L
                );

        SettlementTarget existingTarget =
                mock(SettlementTarget.class);

        when(existingTarget.getId()).thenReturn(1L);

        when(settlementInboxRepository.existsByEventId(eventId))
                .thenReturn(false);

        when(settlementTargetRepository.findByOrderIdAndOrderItemId(
                command.orderId(),
                command.orderItemId()
        )).thenReturn(Optional.of(existingTarget));

        // when
        SettlementEventResult result =
                settlementEventService.receive(command);

        // then
        assertAll(
                () -> assertEquals(eventId, result.eventId()),
                () -> assertEquals(1L, result.settlementTargetId()),
                () -> assertTrue(result.duplicate())
        );

        verify(settlementTargetRepository, never())
                .save(any(SettlementTarget.class));

        verify(settlementInboxRepository).save(
                eventId,
                "PURCHASE_CONFIRMED"
        );
    }

    /**
     * 테스트마다 반복되는 구매확정 Command를 생성합니다.
     */
    private ReceivePurchaseConfirmedCommand createCommand(
            UUID eventId,
            Long orderId,
            Long orderItemId
    ) {
        return new ReceivePurchaseConfirmedCommand(
                eventId,
                orderId,
                orderItemId,
                10L,
                "제주 당근 케이크",
                2,
                new BigDecimal("30000.00"),
                OffsetDateTime.parse(
                        "2026-07-21T10:00:00+09:00"
                )
        );
    }
}