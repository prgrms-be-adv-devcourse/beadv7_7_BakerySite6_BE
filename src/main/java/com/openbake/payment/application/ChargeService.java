package com.openbake.payment.application;

import com.openbake.payment.domain.ChargeRequest;
import com.openbake.payment.domain.ChargeStatus;
import com.openbake.payment.domain.DepositAccount;
import com.openbake.payment.domain.ReferenceType;
import com.openbake.payment.domain.TransactionType;
import com.openbake.payment.domain.WalletTransaction;
import com.openbake.payment.infrastructure.ChargeRequestRepository;
import com.openbake.payment.infrastructure.DepositAccountRepository;
import com.openbake.payment.infrastructure.WalletTransactionRepository;
import com.openbake.payment.presentation.dto.ChargeCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 충전 비즈니스 로직.
 * 각 메서드는 독립적인 트랜잭션 단위다.
 * ChargeFacade가 이 메서드들을 조합해서 전체 충전 흐름을 만든다.
 */
@Service
@RequiredArgsConstructor
public class ChargeService {

    private final ChargeRequestRepository chargeRequestRepository;
    private final DepositAccountRepository depositAccountRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    /**
     * [트랜잭션 1] 충전 요청 생성.
     * ChargeRequest를 READY 상태로 만들고, 프론트가 PG 결제창을 띄우는 데 필요한 정보를 반환한다.
     */
    @Transactional
    public ChargeCreateResponse createChargeRequest(Long memberId, BigDecimal amount) {
        // 이미 진행 중인 충전 요청이 있으면 중복 방지
        boolean hasActiveRequest = chargeRequestRepository.existsByMemberIdAndStatusIn(
                memberId, List.of(ChargeStatus.READY, ChargeStatus.IN_PROGRESS)
        );
        if (hasActiveRequest) {
            throw new IllegalStateException("이미 진행 중인 충전 요청이 있습니다.");
        }

        // PG에 보낼 주문번호 생성 (UUID)
        String pgOrderId = UUID.randomUUID().toString();

        ChargeRequest request = ChargeRequest.create(memberId, amount, pgOrderId);
        chargeRequestRepository.save(request);

        return new ChargeCreateResponse(request.getId(), pgOrderId, amount);
    }

    /**
     * [트랜잭션 2-1] PG 승인 요청 전 상태 변경.
     * READY → IN_PROGRESS로 바꾸고 커밋한다.
     * 이후 PG API 호출은 트랜잭션 밖에서 한다.
     */
    @Transactional
    public ChargeRequest markInProgress(String pgOrderId, String pgPaymentKey, Long memberId, BigDecimal amount) {
        ChargeRequest request = chargeRequestRepository.findByPgOrderId(pgOrderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 충전 요청입니다."));

        // 본인 요청인지, 금액이 일치하는지 검증 (위변조 방지)
        request.validateOwner(memberId);
        request.validateAmountMatches(amount);
        request.markInProgress(pgPaymentKey);

        return request;
    }

    /**
     * [트랜잭션 2-2] PG 승인 성공 후 처리.
     * 예치금 증가 + 원장 기록 + ChargeRequest를 DONE으로 변경.
     */
    @Transactional
    public BigDecimal completeCharge(ChargeRequest request, String pgMethod) {
        // ChargeRequest 상태를 DONE으로
        request.markDone(pgMethod);

        // 회원 예치금 계좌에 금액 추가
        DepositAccount account = depositAccountRepository.findByMemberId(request.getMemberId())
                .orElseGet(() -> depositAccountRepository.save(
                        DepositAccount.createMemberAccount(request.getMemberId())
                ));
        account.charge(request.getAmount());

        // 원장에 충전 내역 기록
        walletTransactionRepository.save(WalletTransaction.create(
                account,
                TransactionType.CHARGE,
                request.getAmount(),           // +금액 (돈 들어옴)
                account.getBalance(),          // 충전 후 잔액
                ReferenceType.CHARGE_REQUEST,
                request.getId()
        ));

        return account.getBalance();
    }

    /**
     * [트랜잭션 2-2] PG 승인 실패 후 처리.
     * ChargeRequest를 FAILED로 변경하고 실패 사유를 기록한다.
     */
    @Transactional
    public void failCharge(ChargeRequest request, String failureCode, String failureReason) {
        request.markFailed(failureCode, failureReason);
    }

    /**
     * 배치용 — 30분 초과된 READY 상태 충전 요청을 만료 처리.
     */
    @Transactional
    public void expireStaleRequests() {
        List<ChargeRequest> readyRequests = chargeRequestRepository.findByStatus(ChargeStatus.READY);
        for (ChargeRequest request : readyRequests) {
            if (request.isExpired()) {
                request.markExpired();
            }
        }
    }
}
