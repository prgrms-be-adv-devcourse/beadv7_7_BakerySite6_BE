package com.openbake.seller.application;

import com.openbake.common.exception.AccountVerificationExpiredException;
import com.openbake.common.exception.AccountVerificationFailedException;
import com.openbake.common.exception.BusinessVerificationFailedException;
import com.openbake.common.exception.EntityNotFoundException;
import com.openbake.common.exception.InvalidSettlementAccountException;
import com.openbake.member.infrastructure.CurrentMemberProvider;
import com.openbake.seller.domain.AccountVerificationRepository;
import com.openbake.seller.domain.AccountVerificationSession;
import com.openbake.seller.domain.VerifiedAccount;
import com.openbake.seller.infrastructure.MockBankRegistry;
import com.openbake.seller.infrastructure.MockBusinessRegistry;
import com.openbake.seller.presentation.dto.AccountVerificationCodeResponse;
import com.openbake.seller.presentation.dto.AccountVerificationConfirmRequest;
import com.openbake.seller.presentation.dto.AccountVerificationConfirmResponse;
import com.openbake.seller.presentation.dto.AccountVerificationStartRequest;
import com.openbake.seller.presentation.dto.AccountVerificationStartResponse;
import com.openbake.seller.presentation.dto.BusinessVerificationRequest;
import com.openbake.seller.presentation.dto.BusinessVerificationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private MockBusinessRegistry mockBusinessRegistry;

    @Mock
    private MockBankRegistry mockBankRegistry;

    @Mock
    private CurrentMemberProvider currentMemberProvider;

    @Mock
    private AccountVerificationRepository accountVerificationRepository;

    @InjectMocks
    private SellerService sellerService;

    @Test
    @DisplayName("등록된 사업자 정보와 일치하면 인증에 성공한다")
    void verifyBusiness_success() {
        BusinessVerificationRequest request =
                new BusinessVerificationRequest("123-45-67890", "서울시", "이세종");
        given(mockBusinessRegistry.isRegistered("123-45-67890", "이세종")).willReturn(true);

        BusinessVerificationResponse response = sellerService.verifyBusiness(request);

        assertThat(response.verified()).isTrue();
        assertThat(response.businessNumber()).isEqualTo("123-45-67890");
    }

    @Test
    @DisplayName("등록된 사업자 정보와 불일치하면 예외가 발생한다")
    void verifyBusiness_fail() {
        BusinessVerificationRequest request =
                new BusinessVerificationRequest("123-45-67890", "서울시", "홍길동");
        given(mockBusinessRegistry.isRegistered("123-45-67890", "홍길동")).willReturn(false);

        assertThatThrownBy(() -> sellerService.verifyBusiness(request))
                .isInstanceOf(BusinessVerificationFailedException.class);
    }

    @Test
    @DisplayName("등록되지 않은 은행 코드면 예외가 발생한다")
    void requestAccountVerification_invalidBankCode() {
        AccountVerificationStartRequest request =
                new AccountVerificationStartRequest("999", "1101234567", "이세종");
        given(mockBankRegistry.isValidBankCode("999")).willReturn(false);

        assertThatThrownBy(() -> sellerService.requestAccountVerification(request))
                .isInstanceOf(InvalidSettlementAccountException.class);
    }

    @Test
    @DisplayName("계좌 인증 요청 성공 시 세션을 저장하고 만료 시각을 반환한다")
    void requestAccountVerification_success() {
        AccountVerificationStartRequest request =
                new AccountVerificationStartRequest("088", "1101234567", "이세종");
        given(mockBankRegistry.isValidBankCode("088")).willReturn(true);
        given(currentMemberProvider.getId()).willReturn(1L);

        AccountVerificationStartResponse response = sellerService.requestAccountVerification(request);

        assertThat(response.verificationRequestId()).isNotBlank();
        assertThat(response.expiresAt()).isAfter(LocalDateTime.now());
        verify(accountVerificationRepository)
                .saveSession(eq(response.verificationRequestId()), any(AccountVerificationSession.class));
    }

    @Test
    @DisplayName("세션이 존재하면 목업 인증 코드를 반환한다")
    void getMockVerificationCode_success() {
        AccountVerificationSession session = new AccountVerificationSession(
                1L, "088", "1101234567", "이세종", "1234", LocalDateTime.now().plusMinutes(3));
        given(accountVerificationRepository.findSession("vr_1")).willReturn(Optional.of(session));

        AccountVerificationCodeResponse response = sellerService.getMockVerificationCode("vr_1");

        assertThat(response.code()).isEqualTo("1234");
    }

    @Test
    @DisplayName("세션이 없으면 목업 인증 코드 조회 시 예외가 발생한다")
    void getMockVerificationCode_notFound() {
        given(accountVerificationRepository.findSession("vr_1")).willReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.getMockVerificationCode("vr_1"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("세션이 없으면 계좌 인증 확인 시 예외가 발생한다")
    void verifyAccount_sessionNotFound() {
        given(currentMemberProvider.getId()).willReturn(1L);
        given(accountVerificationRepository.findSession("vr_1")).willReturn(Optional.empty());

        AccountVerificationConfirmRequest request = new AccountVerificationConfirmRequest("1234");

        assertThatThrownBy(() -> sellerService.verifyAccount("vr_1", request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("다른 회원이 만든 인증 요청이면 예외가 발생한다")
    void verifyAccount_otherMember() {
        given(currentMemberProvider.getId()).willReturn(2L);
        AccountVerificationSession session = new AccountVerificationSession(
                1L, "088", "1101234567", "이세종", "1234", LocalDateTime.now().plusMinutes(3));
        given(accountVerificationRepository.findSession("vr_1")).willReturn(Optional.of(session));

        AccountVerificationConfirmRequest request = new AccountVerificationConfirmRequest("1234");

        assertThatThrownBy(() -> sellerService.verifyAccount("vr_1", request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("인증 유효 시간이 지나면 예외가 발생하고 세션이 삭제된다")
    void verifyAccount_expired() {
        given(currentMemberProvider.getId()).willReturn(1L);
        AccountVerificationSession session = new AccountVerificationSession(
                1L, "088", "1101234567", "이세종", "1234", LocalDateTime.now().minusMinutes(1));
        given(accountVerificationRepository.findSession("vr_1")).willReturn(Optional.of(session));

        AccountVerificationConfirmRequest request = new AccountVerificationConfirmRequest("1234");

        assertThatThrownBy(() -> sellerService.verifyAccount("vr_1", request))
                .isInstanceOf(AccountVerificationExpiredException.class);
        verify(accountVerificationRepository).deleteSession("vr_1");
    }

    @Test
    @DisplayName("인증 코드가 일치하지 않으면 예외가 발생한다")
    void verifyAccount_codeMismatch() {
        given(currentMemberProvider.getId()).willReturn(1L);
        AccountVerificationSession session = new AccountVerificationSession(
                1L, "088", "1101234567", "이세종", "1234", LocalDateTime.now().plusMinutes(3));
        given(accountVerificationRepository.findSession("vr_1")).willReturn(Optional.of(session));

        AccountVerificationConfirmRequest request = new AccountVerificationConfirmRequest("9999");

        assertThatThrownBy(() -> sellerService.verifyAccount("vr_1", request))
                .isInstanceOf(AccountVerificationFailedException.class);
    }

    @Test
    @DisplayName("인증 성공 시 계좌 정보를 memberId 기준으로 저장하고 세션을 삭제한다")
    void verifyAccount_success() {
        given(currentMemberProvider.getId()).willReturn(1L);
        AccountVerificationSession session = new AccountVerificationSession(
                1L, "088", "1101234567", "이세종", "1234", LocalDateTime.now().plusMinutes(3));
        given(accountVerificationRepository.findSession("vr_1")).willReturn(Optional.of(session));

        AccountVerificationConfirmRequest request = new AccountVerificationConfirmRequest("1234");

        AccountVerificationConfirmResponse response = sellerService.verifyAccount("vr_1", request);

        assertThat(response.verified()).isTrue();
        verify(accountVerificationRepository).saveVerifiedAccount(eq(1L), any(VerifiedAccount.class));
        verify(accountVerificationRepository).deleteSession("vr_1");
    }
}
