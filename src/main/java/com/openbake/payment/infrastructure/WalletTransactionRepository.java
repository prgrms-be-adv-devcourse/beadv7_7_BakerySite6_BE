package com.openbake.payment.infrastructure;

import com.openbake.payment.domain.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    Page<WalletTransaction> findByDepositAccountId(Long depositAccountId, Pageable pageable);
}
