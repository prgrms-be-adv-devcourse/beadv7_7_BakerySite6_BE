package com.openbake.settlement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettlementLineTest {

    private static final String EVENT_ID =
            "c41f55a8-9246-4bd6-bdf7-87b109fdb0c1";

    private static final Long SETTLEMENT_ID = 100L;
    private static final Long TARGET_ID = 1L;
    private static final Long ORDER_ID = 1001L;
    private static final Long ORDER_ITEM_ID = 2001L;
    private static final Long SELLER_ID = 10L;
    private static final Long DROP_ID = 3001L;

    private static final OffsetDateTime PURCHASE_CONFIRMED_AT =
            OffsetDateTime.parse("2026-07-21T10:00:00+09:00");

    @Test
    @DisplayName("SettlementTarget의 스냅샷으로 SettlementLine을 생성한다")
    void createSettlementLineFromTarget() {
        // given
        SettlementTarget target = createSavedTarget();

        // when
        SettlementLine line =
                SettlementLine.from(SETTLEMENT_ID, target);

        // then
        assertThat(line.getSettlementId())
                .isEqualTo(SETTLEMENT_ID);

        assertThat(line.getTargetId())
                .isEqualTo(TARGET_ID);

        assertThat(line.getOrderId())
                .isEqualTo(ORDER_ID);

        assertThat(line.getOrderItemId())
                .isEqualTo(ORDER_ITEM_ID);

        assertThat(line.getDropId())
                .isEqualTo(DROP_ID);

        assertThat(line.getProductNameSnapshot())
                .isEqualTo("제주 당근 케이크");

        assertThat(line.getQuantity())
                .isEqualTo(2);

        assertThat(line.getGrossAmount())
                .isEqualByComparingTo("30000.00");

        assertThat(line.getCommissionRateSnapshot())
                .isEqualByComparingTo("0.1000");

        assertThat(line.getCommissionAmount())
                .isEqualByComparingTo("3000.00");

        assertThat(line.getNetAmount())
                .isEqualByComparingTo("27000.00");

        assertThat(line.getPurchaseConfirmedAt())
                .isEqualTo(PURCHASE_CONFIRMED_AT);

        assertThat(line.getCreatedAt())
                .isNotNull();
    }

    @Test
    @DisplayName("저장되지 않은 SettlementTarget은 SettlementLine으로 만들 수 없다")
    void rejectUnsavedTarget() {
        // given
        SettlementTarget target = createTarget();

        // when & then
        assertThatThrownBy(
                () -> SettlementLine.from(SETTLEMENT_ID, target)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "저장된 SettlementTarget만 정산 상세로 만들 수 있습니다."
                );
    }

    @Test
    @DisplayName("SettlementTarget이 null이면 SettlementLine을 만들 수 없다")
    void rejectNullTarget() {
        assertThatThrownBy(
                () -> SettlementLine.from(SETTLEMENT_ID, null)
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("SettlementTarget은 필수입니다.");
    }

    @Test
    @DisplayName("settlementId는 0보다 커야 한다")
    void rejectInvalidSettlementId() {
        // given
        SettlementTarget target = createSavedTarget();

        // when & then
        assertThatThrownBy(
                () -> SettlementLine.from(0L, target)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("settlementId는 0보다 커야 합니다.");
    }

    private SettlementTarget createSavedTarget() {
        SettlementTarget target = createTarget();

        ReflectionTestUtils.setField(
                target,
                "id",
                TARGET_ID
        );

        return target;
    }

    private SettlementTarget createTarget() {
        return SettlementTarget.create(
                EVENT_ID,
                ORDER_ID,
                ORDER_ITEM_ID,
                SELLER_ID,
                DROP_ID,
                "제주 당근 케이크",
                2,
                new BigDecimal("30000.00"),
                new BigDecimal("0.1000"),
                PURCHASE_CONFIRMED_AT
        );
    }
}