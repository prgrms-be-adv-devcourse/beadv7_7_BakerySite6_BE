package com.openbake.payment.application;

import com.openbake.payment.domain.ChargeRequest;
import com.openbake.payment.domain.ChargeStatus;
import com.openbake.payment.domain.DepositAccount;
import com.openbake.payment.infrastructure.ChargeRequestRepository;
import com.openbake.payment.infrastructure.DepositAccountRepository;
import com.openbake.payment.infrastructure.pg.PgApproveException;
import com.openbake.payment.infrastructure.pg.PgApproveResponse;
import com.openbake.payment.infrastructure.pg.PgClient;
import com.openbake.payment.presentation.dto.ChargeApproveResponse;
import com.openbake.payment.presentation.dto.ChargeCreateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

/**
 * ChargeFacade 테스트.
 * PgClient를 Mock으로 대체해서 실제 토스 API를 호출하지 않고 테스트한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChargeFacadeTest {

    @Autowired
    private ChargeFacade chargeFacade;

    @Autowired
    private ChargeService chargeService;

    @MockitoBean
    private PgClient pgClient;

    @Autowired
    private ChargeRequestRepository chargeRequestRepository;

    @Autowired
    private DepositAccountRepository depositAccountRepository;

    @Test
    @DisplayName("PG 승인 성공 시 예치금이 증가하고 DONE 상태가 된다")
    void approveSuccess() {
        // given — 회원 계좌 (잔액 5000원)
        DepositAccount account = DepositAccount.createMemberAccount(1L);
        account.charge(new BigDecimal("5000"));
        depositAccountRepository.save(account);

        // 충전 요청 생성
        ChargeCreateResponse created = chargeService.createChargeRequest(1L, new BigDecimal("10000"));

        // PG Mock — 승인 성공 응답
        given(pgClient.approve(eq("payment_key_123"), eq(created.pgOrderId()), any()))
                .willReturn(new PgApproveResponse("payment_key_123", created.pgOrderId(), "카드", "DONE"));

        // when
        ChargeApproveResponse response = chargeFacade.approve(
                1L, "payment_key_123", created.pgOrderId(), new BigDecimal("10000"));

        // then — 잔액: 5000 + 10000 = 15000
        assertThat(response.chargedAmount()).isEqualByComparingTo("10000");
        assertThat(response.balanceAfter()).isEqualByComparingTo("15000");

        // ChargeRequest가 DONE 상태인지 확인
        ChargeRequest request = chargeRequestRepository.findById(created.chargeRequestId()).get();
        assertThat(request.getStatus()).isEqualTo(ChargeStatus.DONE);
    }

    @Test
    @DisplayName("PG 승인 실패 시 FAILED 상태가 되고 예외가 발생한다")
    void approveFailure() {
        // given
        DepositAccount account = DepositAccount.createMemberAccount(1L);
        depositAccountRepository.save(account);

        ChargeCreateResponse created = chargeService.createChargeRequest(1L, new BigDecimal("10000"));

        // PG Mock — 승인 거절
        given(pgClient.approve(eq("payment_key_fail"), eq(created.pgOrderId()), any()))
                .willThrow(new PgApproveException("CARD_LIMIT_EXCEEDED", "카드 한도 초과"));

        // when & then
        assertThatThrownBy(() ->
                chargeFacade.approve(1L, "payment_key_fail", created.pgOrderId(), new BigDecimal("10000"))
        ).isInstanceOf(PgApproveException.class);

        // ChargeRequest가 FAILED 상태인지 확인
        ChargeRequest request = chargeRequestRepository.findById(created.chargeRequestId()).get();
        assertThat(request.getStatus()).isEqualTo(ChargeStatus.FAILED);
        assertThat(request.getFailureCode()).isEqualTo("CARD_LIMIT_EXCEEDED");
    }
}
