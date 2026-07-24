package com.openbake.seller.application;

import com.openbake.common.exception.AccountVerificationExpiredException;
import com.openbake.common.exception.AccountVerificationFailedException;
import com.openbake.common.exception.BusinessVerificationFailedException;
import com.openbake.common.exception.EntityNotFoundException;
import com.openbake.common.exception.InvalidSettlementAccountException;
import com.openbake.common.security.CurrentMemberProvider;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerService {

    private final MockBusinessRegistry mockBusinessRegistry;
    private final MockBankRegistry mockBankRegistry;
    private final CurrentMemberProvider currentMemberProvider;
    private final AccountVerificationRepository accountVerificationRepository;

    public BusinessVerificationResponse verifyBusiness(BusinessVerificationRequest request) {
        boolean verified = mockBusinessRegistry.isRegistered(request.businessNumber(), request.businessRepresentativeName());
        log.info("[MOCK] 사업자 인증 시도 - businessNumber={}, verified={}", request.businessNumber(), verified);

        if (!verified) {
            throw new BusinessVerificationFailedException();
        }

        return new BusinessVerificationResponse(true, request.businessNumber(), LocalDateTime.now());
    }

    public AccountVerificationStartResponse requestAccountVerification(AccountVerificationStartRequest request) {
        if (!mockBankRegistry.isValidBankCode(request.bankCode())) {
            throw new InvalidSettlementAccountException();
        }

        Long memberId = currentMemberProvider.getId();

        String verificationRequestId = "vr_" + UUID.randomUUID();
        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(3);

        AccountVerificationSession session = new AccountVerificationSession(
                memberId, request.bankCode(), request.accountNumber(), request.accountHolder(), code, expiresAt);
        accountVerificationRepository.saveSession(verificationRequestId, session);

        // [목업 안내] 실제 1원 송금 대신 서버 로그로만 코드를 남김. DEV 환경에선 getMockVerificationCode API로 조회.
        log.info("[MOCK] 계좌 인증 코드 발급 - verificationRequestId={}, code={}", verificationRequestId, code);

        return new AccountVerificationStartResponse(verificationRequestId, expiresAt);
    }

    public AccountVerificationCodeResponse getMockVerificationCode(String verificationRequestId) {
        AccountVerificationSession session = accountVerificationRepository.findSession(verificationRequestId)
                .orElseThrow(() -> new EntityNotFoundException("대상을 찾을 수 없습니다."));

        return new AccountVerificationCodeResponse(verificationRequestId, session.code(), session.expiresAt());
    }

    public AccountVerificationConfirmResponse verifyAccount(
            String verificationRequestId, AccountVerificationConfirmRequest request) {
        Long memberId = currentMemberProvider.getId();

        AccountVerificationSession session = accountVerificationRepository.findSession(verificationRequestId)
                .orElseThrow(() -> new EntityNotFoundException("대상을 찾을 수 없습니다."));

        if (!session.memberId().equals(memberId)) {
            throw new EntityNotFoundException("대상을 찾을 수 없습니다.");
        }

        if (session.expiresAt().isBefore(LocalDateTime.now())) {
            accountVerificationRepository.deleteSession(verificationRequestId);
            throw new AccountVerificationExpiredException();
        }

        if (!session.code().equals(request.verificationCode())) {
            throw new AccountVerificationFailedException();
        }

        LocalDateTime verifiedAt = LocalDateTime.now();
        VerifiedAccount verifiedAccount = new VerifiedAccount(
                session.bankCode(), session.accountNumber(), session.accountHolder(), verifiedAt);
        accountVerificationRepository.saveVerifiedAccount(memberId, verifiedAccount);
        accountVerificationRepository.deleteSession(verificationRequestId);

        return new AccountVerificationConfirmResponse(true, verifiedAt);
    }

    private String generateCode() {
        return String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
