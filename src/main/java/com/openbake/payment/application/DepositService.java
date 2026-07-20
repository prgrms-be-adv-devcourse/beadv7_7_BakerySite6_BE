package com.openbake.payment.application;

import com.openbake.payment.domain.DepositAccount;
import com.openbake.payment.infrastructure.DepositAccountRepository;
import com.openbake.payment.presentation.dto.DepositResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final DepositAccountRepository depositAccountRepository;

    /**
     * 예치금 잔액 조회.
     * 계좌가 없으면 자동 생성(잔액 0원)해서 반환한다.
     * → 아직 충전한 적 없는 신규 회원도 조회 가능.
     */
    @Transactional(readOnly = true)
    public DepositResponse getBalance(Long memberId) {
        DepositAccount account = depositAccountRepository.findByMemberId(memberId)
                .orElseGet(() -> depositAccountRepository.save(
                        DepositAccount.createMemberAccount(memberId)
                ));

        return new DepositResponse(memberId, account.getBalance());
    }
}
