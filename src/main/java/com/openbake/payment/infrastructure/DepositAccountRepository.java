package com.openbake.payment.infrastructure;

import com.openbake.payment.domain.AccountType;
import com.openbake.payment.domain.DepositAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepositAccountRepository extends JpaRepository<DepositAccount, Long> {

    Optional<DepositAccount> findByMemberId(Long memberId);

    boolean existsByAccountType(AccountType accountType);

    Optional<DepositAccount> findByAccountType(AccountType accountType);
}
