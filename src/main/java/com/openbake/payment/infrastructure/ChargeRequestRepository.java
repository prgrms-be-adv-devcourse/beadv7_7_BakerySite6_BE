package com.openbake.payment.infrastructure;

import com.openbake.payment.domain.ChargeRequest;
import com.openbake.payment.domain.ChargeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChargeRequestRepository extends JpaRepository<ChargeRequest, Long> {

    Optional<ChargeRequest> findByPgOrderId(String pgOrderId);

    Optional<ChargeRequest> findByPgPaymentKey(String pgPaymentKey);

    boolean existsByMemberIdAndStatusIn(Long memberId, List<ChargeStatus> statuses);

    List<ChargeRequest> findByStatus(ChargeStatus status);
}
