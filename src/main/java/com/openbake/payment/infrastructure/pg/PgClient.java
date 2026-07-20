package com.openbake.payment.infrastructure.pg;

import java.math.BigDecimal;

/**
 * PG사 API 호출 인터페이스.
 * 현재는 토스페이먼츠만 사용하지만, 인터페이스로 분리해서
 * 나중에 다른 PG사로 교체하거나 테스트용 Mock을 끼울 수 있게 한다.
 */
public interface PgClient {

    /**
     * PG 승인 요청.
     * 프론트에서 받은 paymentKey + orderId + amount로 PG에 최종 승인을 요청한다.
     * 승인이 실패하면 PgApproveException을 던진다.
     */
    PgApproveResponse approve(String pgPaymentKey, String pgOrderId, BigDecimal amount);
}
