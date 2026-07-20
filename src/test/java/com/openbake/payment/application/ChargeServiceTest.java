package com.openbake.payment.application;

import com.openbake.payment.domain.ChargeRequest;
import com.openbake.payment.domain.ChargeStatus;
import com.openbake.payment.domain.DepositAccount;
import com.openbake.payment.infrastructure.ChargeRequestRepository;
import com.openbake.payment.infrastructure.DepositAccountRepository;
import com.openbake.payment.infrastructure.WalletTransactionRepository;
import com.openbake.payment.presentation.dto.ChargeCreateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChargeServiceTest {

    @Autowired
    private ChargeService chargeService;

    @Autowired
    private ChargeRequestRepository chargeRequestRepository;

    @Autowired
    private DepositAccountRepository depositAccountRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Nested
    @DisplayName("createChargeRequest()")
    class CreateChargeRequest {

        @Test
        @DisplayName("충전 요청이 READY 상태로 생성된다")
        void createsInReadyStatus() {
            ChargeCreateResponse response = chargeService.createChargeRequest(1L, new BigDecimal("10000"));

            assertThat(response.amount()).isEqualByComparingTo("10000");
            assertThat(response.pgOrderId()).isNotBlank();

            // DB에서 확인
            ChargeRequest saved = chargeRequestRepository.findById(response.chargeRequestId()).get();
            assertThat(saved.getStatus()).isEqualTo(ChargeStatus.READY);
            assertThat(saved.getMemberId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("이미 진행 중인 충전 요청이 있으면 예외가 발생한다")
        void failsWhenActiveRequestExists() {
            chargeService.createChargeRequest(1L, new BigDecimal("10000"));

            assertThatThrownBy(() ->
                    chargeService.createChargeRequest(1L, new BigDecimal("20000"))
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 진행 중인 충전 요청");
        }
    }

    @Nested
    @DisplayName("markInProgress()")
    class MarkInProgress {

        @Test
        @DisplayName("READY → IN_PROGRESS로 변경된다")
        void changesStatusToInProgress() {
            ChargeCreateResponse created = chargeService.createChargeRequest(1L, new BigDecimal("10000"));

            ChargeRequest request = chargeService.markInProgress(
                    created.pgOrderId(), "payment_key_123", 1L, new BigDecimal("10000"));

            assertThat(request.getStatus()).isEqualTo(ChargeStatus.IN_PROGRESS);
            assertThat(request.getPgPaymentKey()).isEqualTo("payment_key_123");
        }

        @Test
        @DisplayName("본인 요청이 아니면 예외가 발생한다")
        void failsWhenNotOwner() {
            ChargeCreateResponse created = chargeService.createChargeRequest(1L, new BigDecimal("10000"));

            assertThatThrownBy(() ->
                    chargeService.markInProgress(
                            created.pgOrderId(), "payment_key_123", 999L, new BigDecimal("10000"))
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("본인의 충전 요청이 아닙니다");
        }

        @Test
        @DisplayName("금액이 불일치하면 예외가 발생한다")
        void failsWhenAmountMismatch() {
            ChargeCreateResponse created = chargeService.createChargeRequest(1L, new BigDecimal("10000"));

            assertThatThrownBy(() ->
                    chargeService.markInProgress(
                            created.pgOrderId(), "payment_key_123", 1L, new BigDecimal("99999"))
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("충전 금액이 일치하지 않습니다");
        }
    }

    @Nested
    @DisplayName("completeCharge()")
    class CompleteCharge {

        @Test
        @DisplayName("충전 완료 시 예치금이 증가하고 원장에 기록된다")
        void increasesBalanceAndRecordsTransaction() {
            // given — 충전 요청 생성 + IN_PROGRESS 전환
            ChargeCreateResponse created = chargeService.createChargeRequest(1L, new BigDecimal("10000"));
            ChargeRequest request = chargeService.markInProgress(
                    created.pgOrderId(), "payment_key_123", 1L, new BigDecimal("10000"));

            // 회원 계좌 미리 생성 (잔액 5000원)
            DepositAccount account = DepositAccount.createMemberAccount(1L);
            account.charge(new BigDecimal("5000"));
            depositAccountRepository.save(account);

            // when
            BigDecimal balanceAfter = chargeService.completeCharge(request, "카드");

            // then — 잔액: 5000 + 10000 = 15000
            assertThat(balanceAfter).isEqualByComparingTo("15000");
            assertThat(request.getStatus()).isEqualTo(ChargeStatus.DONE);

            // 원장 기록 확인
            assertThat(walletTransactionRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("계좌가 없으면 자동 생성 후 충전된다")
        void createsAccountIfNotExists() {
            ChargeCreateResponse created = chargeService.createChargeRequest(2L, new BigDecimal("10000"));
            ChargeRequest request = chargeService.markInProgress(
                    created.pgOrderId(), "payment_key_456", 2L, new BigDecimal("10000"));

            BigDecimal balanceAfter = chargeService.completeCharge(request, "카드");

            // 계좌 자동 생성 + 0 + 10000 = 10000
            assertThat(balanceAfter).isEqualByComparingTo("10000");
            assertThat(depositAccountRepository.findByMemberId(2L)).isPresent();
        }
    }

    @Nested
    @DisplayName("failCharge()")
    class FailCharge {

        @Test
        @DisplayName("실패 시 FAILED 상태로 변경되고 사유가 기록된다")
        void marksAsFailed() {
            ChargeCreateResponse created = chargeService.createChargeRequest(1L, new BigDecimal("10000"));
            ChargeRequest request = chargeService.markInProgress(
                    created.pgOrderId(), "payment_key_123", 1L, new BigDecimal("10000"));

            chargeService.failCharge(request, "CARD_LIMIT_EXCEEDED", "카드 한도 초과");

            assertThat(request.getStatus()).isEqualTo(ChargeStatus.FAILED);
            assertThat(request.getFailureCode()).isEqualTo("CARD_LIMIT_EXCEEDED");
            assertThat(request.getFailureReason()).isEqualTo("카드 한도 초과");
        }
    }

    @Nested
    @DisplayName("expireStaleRequests()")
    class ExpireStaleRequests {

        @Test
        @DisplayName("만료 시간이 지나지 않은 READY 요청은 그대로 유지된다")
        void keepsNonExpiredRequests() {
            chargeService.createChargeRequest(1L, new BigDecimal("10000"));

            chargeService.expireStaleRequests();

            // 방금 만든 건 30분 안 지났으므로 READY 유지
            ChargeRequest request = chargeRequestRepository.findAll().get(0);
            assertThat(request.getStatus()).isEqualTo(ChargeStatus.READY);
        }
    }
}
