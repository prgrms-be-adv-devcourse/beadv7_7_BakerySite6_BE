package com.openbake.settlement.application;

import com.openbake.settlement.domain.SettlementTarget;
import com.openbake.settlement.domain.SettlementTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 구매확정 이벤트를 처리하는 애플리케이션 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SettlementEventService {

    /**
     * 구매확정 이벤트 유형입니다.
     */
    private static final String PURCHASE_CONFIRMED_EVENT =
            "PURCHASE_CONFIRMED";

    /**
     * MVP에서 사용하는 기본 정산 수수료율입니다.
     *
     * 0.1000은 10%를 의미합니다.
     */
    private static final BigDecimal DEFAULT_COMMISSION_RATE =
            new BigDecimal("0.1000");

    private final SettlementInboxRepository settlementInboxRepository;
    private final SettlementTargetRepository settlementTargetRepository;

    /**
     * 구매확정 이벤트를 받아 정산 대상을 생성합니다.
     *
     * 동일한 eventId 또는 동일한 주문 상품이 이미 처리됐다면
     * 새로운 정산 대상을 만들지 않고 중복 처리 결과를 반환합니다.
     */
    public SettlementEventResult receive(
            ReceivePurchaseConfirmedCommand command
    ) {
        Objects.requireNonNull(command, "command는 필수입니다.");
        Objects.requireNonNull(command.eventId(), "eventId는 필수입니다.");

        /*
         * 같은 이벤트가 다시 전달된 경우입니다.
         */
        if (settlementInboxRepository.existsByEventId(command.eventId())) {
            Long existingTargetId = settlementTargetRepository
                    .findByOrderIdAndOrderItemId(
                            command.orderId(),
                            command.orderItemId()
                    )
                    .map(SettlementTarget::getId)
                    .orElse(null);

            return SettlementEventResult.duplicated(
                    command.eventId(),
                    existingTargetId
            );
        }

        /*
         * eventId는 다르지만 동일한 주문 상품이 이미
         * 정산 대상으로 등록된 경우입니다.
         */
        var existingTarget = settlementTargetRepository
                .findByOrderIdAndOrderItemId(
                        command.orderId(),
                        command.orderItemId()
                );

        if (existingTarget.isPresent()) {
            settlementInboxRepository.save(
                    command.eventId(),
                    PURCHASE_CONFIRMED_EVENT
            );

            return SettlementEventResult.duplicated(
                    command.eventId(),
                    existingTarget.get().getId()
            );
        }

        /*
         * 새로운 정산 대상을 생성합니다.
         */
        SettlementTarget settlementTarget =
                SettlementTarget.create(
                        command.orderId(),
                        command.orderItemId(),
                        command.sellerId(),
                        command.productName(),
                        command.quantity(),
                        command.grossAmount(),
                        DEFAULT_COMMISSION_RATE,
                        command.confirmedAt()
                );

        SettlementTarget savedTarget =
                settlementTargetRepository.save(settlementTarget);

        /*
         * 정산 대상 저장이 성공한 이벤트를 Inbox에 기록합니다.
         *
         * 이 작업들은 같은 트랜잭션에서 실행되므로 중간에 실패하면
         * SettlementTarget과 Inbox 저장이 함께 롤백됩니다.
         */
        settlementInboxRepository.save(
                command.eventId(),
                PURCHASE_CONFIRMED_EVENT
        );

        return SettlementEventResult.created(
                command.eventId(),
                savedTarget.getId()
        );
    }
}