package com.openbake.settlement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettlementTest {

    private static final Long SELLER_ID = 10L;

    private static final LocalDate PERIOD_START =
            LocalDate.of(2026, 7, 1);

    private static final LocalDate PERIOD_END =
            LocalDate.of(2026, 8, 1);

    @Test
    @DisplayName("판매자별 월 정산을 생성한다")
    void createSettlement() {
        // when
        Settlement settlement = createDefaultSettlement();

        // then
        assertThat(settlement.getSellerId())
                .isEqualTo(SELLER_ID);

        assertThat(settlement.getPeriodStart())
                .isEqualTo(PERIOD_START);

        assertThat(settlement.getPeriodEnd())
                .isEqualTo(PERIOD_END);

        assertThat(settlement.getGrossSalesAmount())
                .isEqualByComparingTo("500000.00");

        assertThat(settlement.getCommissionAmount())
                .isEqualByComparingTo("50000.00");

        assertThat(settlement.getNetSalesAmount())
                .isEqualByComparingTo("450000.00");

        assertThat(settlement.getAdjustmentAmount())
                .isEqualByComparingTo("-10000.00");

        assertThat(settlement.getPayoutAmount())
                .isEqualByComparingTo("440000.00");

        assertThat(settlement.getTargetCount())
                .isEqualTo(20);

        assertThat(settlement.getStatus())
                .isEqualTo(SettlementStatus.READY);

        assertThat(settlement.getCreatedAt())
                .isNotNull();

        assertThat(settlement.getUpdatedAt())
                .isNotNull();

        assertThat(settlement.getCompletedAt())
                .isNull();
    }

    @Test
    @DisplayName("보정 금액이 없으면 순정산 금액과 지급 예정 금액이 같다")
    void payoutAmountWithoutAdjustment() {
        // when
        Settlement settlement = Settlement.create(
                SELLER_ID,
                PERIOD_START,
                PERIOD_END,
                new BigDecimal("500000.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("0.00"),
                20
        );

        // then
        assertThat(settlement.getNetSalesAmount())
                .isEqualByComparingTo("450000.00");

        assertThat(settlement.getPayoutAmount())
                .isEqualByComparingTo("450000.00");
    }

    @Test
    @DisplayName("추가 보정 금액은 최종 지급 예정 금액에 더한다")
    void addPositiveAdjustment() {
        // when
        Settlement settlement = Settlement.create(
                SELLER_ID,
                PERIOD_START,
                PERIOD_END,
                new BigDecimal("500000.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("10000.00"),
                20
        );

        // then
        assertThat(settlement.getNetSalesAmount())
                .isEqualByComparingTo("450000.00");

        assertThat(settlement.getPayoutAmount())
                .isEqualByComparingTo("460000.00");
    }

    @Test
    @DisplayName("READY 상태의 정산은 지급을 시작할 수 있다")
    void startPaying() {
        // given
        Settlement settlement = createDefaultSettlement();

        // when
        settlement.startPaying();

        // then
        assertThat(settlement.getStatus())
                .isEqualTo(SettlementStatus.PAYING);

        assertThat(settlement.getUpdatedAt())
                .isNotNull();
    }

    @Test
    @DisplayName("지급 실패 후 다시 지급을 시작할 수 있다")
    void retryPaymentAfterFailure() {
        // given
        Settlement settlement = createDefaultSettlement();

        settlement.startPaying();
        settlement.failPayment();

        assertThat(settlement.getStatus())
                .isEqualTo(SettlementStatus.FAILED);

        // when
        settlement.startPaying();

        // then
        assertThat(settlement.getStatus())
                .isEqualTo(SettlementStatus.PAYING);
    }

    @Test
    @DisplayName("PAYING 상태의 정산은 지급 실패 상태로 변경할 수 있다")
    void failPayment() {
        // given
        Settlement settlement = createDefaultSettlement();
        settlement.startPaying();

        // when
        settlement.failPayment();

        // then
        assertThat(settlement.getStatus())
                .isEqualTo(SettlementStatus.FAILED);
    }

    @Test
    @DisplayName("PAYING 상태의 정산은 완료할 수 있다")
    void completeSettlement() {
        // given
        Settlement settlement = createDefaultSettlement();
        settlement.startPaying();

        // when
        settlement.complete();

        // then
        assertThat(settlement.getStatus())
                .isEqualTo(SettlementStatus.COMPLETED);

        assertThat(settlement.getCompletedAt())
                .isNotNull();

        assertThat(settlement.getUpdatedAt())
                .isNotNull();
    }

    @Test
    @DisplayName("READY 상태의 정산은 지급 보류할 수 있다")
    void holdSettlement() {
        // given
        Settlement settlement = createDefaultSettlement();

        // when
        settlement.hold();

        // then
        assertThat(settlement.getStatus())
                .isEqualTo(SettlementStatus.ON_HOLD);
    }

    @Test
    @DisplayName("지급 보류 상태를 해제하면 READY 상태가 된다")
    void releaseHold() {
        // given
        Settlement settlement = createDefaultSettlement();
        settlement.hold();

        // when
        settlement.releaseHold();

        // then
        assertThat(settlement.getStatus())
                .isEqualTo(SettlementStatus.READY);
    }

    @Test
    @DisplayName("PAYING 상태가 아니면 정산을 완료할 수 없다")
    void cannotCompleteWhenNotPaying() {
        // given
        Settlement settlement = createDefaultSettlement();

        // when & then
        assertThatThrownBy(settlement::complete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "PAYING 상태에서만 정산을 완료할 수 있습니다."
                );
    }

    @Test
    @DisplayName("PAYING 상태가 아니면 지급 실패 처리할 수 없다")
    void cannotFailWhenNotPaying() {
        // given
        Settlement settlement = createDefaultSettlement();

        // when & then
        assertThatThrownBy(settlement::failPayment)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "PAYING 상태에서만 지급 실패 처리할 수 있습니다."
                );
    }

    @Test
    @DisplayName("지급 진행 중인 정산은 보류할 수 없다")
    void cannotHoldPayingSettlement() {
        // given
        Settlement settlement = createDefaultSettlement();
        settlement.startPaying();

        // when & then
        assertThatThrownBy(settlement::hold)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "지급 진행 중인 정산은 보류할 수 없습니다."
                );
    }

    @Test
    @DisplayName("완료된 정산은 보류할 수 없다")
    void cannotHoldCompletedSettlement() {
        // given
        Settlement settlement = createDefaultSettlement();
        settlement.startPaying();
        settlement.complete();

        // when & then
        assertThatThrownBy(settlement::hold)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "완료된 정산은 보류할 수 없습니다."
                );
    }

    @Test
    @DisplayName("정산 시작일은 종료일보다 이전이어야 한다")
    void rejectInvalidPeriod() {
        assertThatThrownBy(() -> Settlement.create(
                SELLER_ID,
                PERIOD_END,
                PERIOD_START,
                new BigDecimal("500000.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("0.00"),
                20
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "periodStart는 periodEnd보다 이전이어야 합니다."
                );
    }

    @Test
    @DisplayName("수수료는 총 판매금액을 초과할 수 없다")
    void rejectCommissionGreaterThanGrossSales() {
        assertThatThrownBy(() -> Settlement.create(
                SELLER_ID,
                PERIOD_START,
                PERIOD_END,
                new BigDecimal("50000.00"),
                new BigDecimal("60000.00"),
                new BigDecimal("0.00"),
                20
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "수수료는 총 판매금액을 초과할 수 없습니다."
                );
    }

    @Test
    @DisplayName("보정 반영 후 지급 예정 금액은 음수가 될 수 없다")
    void rejectNegativePayoutAmount() {
        assertThatThrownBy(() -> Settlement.create(
                SELLER_ID,
                PERIOD_START,
                PERIOD_END,
                new BigDecimal("500000.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("-500000.00"),
                20
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "최종 지급 예정 금액은 0보다 작을 수 없습니다."
                );
    }

    @Test
    @DisplayName("정산 대상 건수는 0보다 커야 한다")
    void rejectZeroTargetCount() {
        assertThatThrownBy(() -> Settlement.create(
                SELLER_ID,
                PERIOD_START,
                PERIOD_END,
                new BigDecimal("500000.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("0.00"),
                0
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "targetCount는 0보다 커야 합니다."
                );
    }

    private Settlement createDefaultSettlement() {
        return Settlement.create(
                SELLER_ID,
                PERIOD_START,
                PERIOD_END,
                new BigDecimal("500000.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("-10000.00"),
                20
        );
    }
}