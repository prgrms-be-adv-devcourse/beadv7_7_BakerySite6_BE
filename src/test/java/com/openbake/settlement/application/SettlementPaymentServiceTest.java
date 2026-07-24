package com.openbake.settlement.application;

import com.openbake.common.exception.EntityNotFoundException;
import com.openbake.settlement.domain.Settlement;
import com.openbake.settlement.domain.SettlementRepository;
import com.openbake.settlement.domain.SettlementStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementPaymentServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    private SettlementPaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService =
                new SettlementPaymentService(
                        settlementRepository
                );
    }

    @Test
    @DisplayName("READY 상태의 정산 지급을 시작한다")
    void startPayment() {
        // given
        Settlement settlement = createSettlement();

        when(settlementRepository.findById(1L))
                .thenReturn(Optional.of(settlement));

        // when
        SettlementPaymentResult result =
                paymentService.start(1L);

        // then
        assertThat(result.status())
                .isEqualTo(
                        SettlementStatus.PAYING.name()
                );

        assertThat(result.updatedAt())
                .isNotNull();

        verify(settlementRepository)
                .findById(1L);
    }

    @Test
    @DisplayName("PAYING 상태의 정산을 완료한다")
    void completePayment() {
        // given
        Settlement settlement = createSettlement();
        settlement.startPaying();

        when(settlementRepository.findById(1L))
                .thenReturn(Optional.of(settlement));

        // when
        SettlementPaymentResult result =
                paymentService.complete(1L);

        // then
        assertThat(result.status())
                .isEqualTo(
                        SettlementStatus.COMPLETED.name()
                );

        assertThat(result.completedAt())
                .isNotNull();
    }

    @Test
    @DisplayName("PAYING 상태의 정산을 지급 실패 처리한다")
    void failPayment() {
        // given
        Settlement settlement = createSettlement();
        settlement.startPaying();

        when(settlementRepository.findById(1L))
                .thenReturn(Optional.of(settlement));

        // when
        SettlementPaymentResult result =
                paymentService.fail(1L);

        // then
        assertThat(result.status())
                .isEqualTo(
                        SettlementStatus.FAILED.name()
                );
    }

    @Test
    @DisplayName("존재하지 않는 정산은 지급 처리할 수 없다")
    void rejectMissingSettlement() {
        // given
        when(settlementRepository.findById(999L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                paymentService.start(999L)
        )
                .isInstanceOf(
                        EntityNotFoundException.class
                )
                .hasMessageContaining(
                        "정산 정보를 찾을 수 없습니다."
                );
    }

    @Test
    @DisplayName("정산 ID는 0보다 커야 한다")
    void rejectInvalidSettlementId() {
        assertThatThrownBy(() ->
                paymentService.start(0L)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "settlementId는 0보다 커야 합니다."
                );
    }

    private Settlement createSettlement() {
        return Settlement.create(
                10L,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 8, 1),
                new BigDecimal("50000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("0.00"),
                2
        );
    }
}