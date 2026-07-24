package com.openbake.payment.presentation.dto;

/**
 * 토스페이먼츠 웹훅 요청 바디.
 * 토스가 결제 상태 변경 시 우리 서버로 POST를 보낸다.
 *
 * 실제 페이로드 구조:
 * { "eventType": "PAYMENT_STATUS_CHANGED", "data": { "paymentKey": "...", "orderId": "...", "status": "DONE" } }
 */
public record TossWebhookRequest(
        String eventType,
        TossWebhookData data
) {
    public record TossWebhookData(
            String paymentKey,
            String orderId,
            String status,
            Integer totalAmount
    ) {}
}
