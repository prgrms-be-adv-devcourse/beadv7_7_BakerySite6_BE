package com.openbake.payment.presentation.dto;

/**
 * 토스페이먼츠 웹훅 요청 바디.
 * 토스가 결제 상태 변경 시 우리 서버로 POST를 보낸다.
 * 실제로는 더 많은 필드가 있지만, 필요한 것만 매핑한다.
 */
public record TossWebhookRequest(
        String eventType,     // "PAYMENT_STATUS_CHANGED" 등
        String paymentKey,
        String orderId,
        String status          // "DONE", "CANCELED" 등
) {
}
