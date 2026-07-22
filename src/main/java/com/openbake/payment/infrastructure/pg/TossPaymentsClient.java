package com.openbake.payment.infrastructure.pg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * 토스페이먼츠 API 클라이언트.
 * RestClient(Spring 6.1+)를 사용한다. (ARD-003: webmvc 환경이므로 WebClient 대신 RestClient)
 *
 * 토스페이먼츠 승인 API 흐름:
 * 1. 프론트에서 결제창 완료 → paymentKey, orderId, amount를 우리 서버에 전달
 * 2. 우리 서버가 이 값으로 토스 승인 API(POST /confirm)를 호출
 * 3. 토스가 승인하면 결제 완료 응답 반환
 *
 * 인증: Basic Auth (시크릿키:빈문자열을 Base64 인코딩)
 */
@Component
public class TossPaymentsClient implements PgClient {

    private final RestClient restClient;

    public TossPaymentsClient(@Value("${payment.toss.secret-key}") String secretKey) {
        // 토스페이먼츠 인증 형식: Base64(시크릿키 + ":")
        String encoded = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        this.restClient = RestClient.builder()
                .baseUrl("https://api.tosspayments.com/v1/payments")
                .defaultHeader("Authorization", "Basic " + encoded)
                .build();
    }

    /**
     * 토스페이먼츠 승인 API 호출.
     * 실패하면 PgApproveException을 던진다.
     */
    @Override
    public PgApproveResponse approve(String pgPaymentKey, String pgOrderId, BigDecimal amount) {
        try {
            return restClient.post()
                    .uri("/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "paymentKey", pgPaymentKey,
                            "orderId", pgOrderId,
                            "amount", amount
                    ))
                    .retrieve()
                    .body(PgApproveResponse.class);
        } catch (Exception e) {
            // TODO: 토스 에러 응답 파싱해서 failureCode/failureReason 추출
            throw new PgApproveException("PG_ERROR", e.getMessage());
        }
    }

    /**
     * 토스페이먼츠 결제 조회 API 호출.
     * GET /v1/payments/{paymentKey}
     * 미결 충전 확인 배치에서 IN_PROGRESS 건의 실제 상태를 확인할 때 사용.
     */
    @Override
    public PgPaymentStatus getPaymentStatus(String pgPaymentKey) {
        return restClient.get()
                .uri("/{paymentKey}", pgPaymentKey)
                .retrieve()
                .body(PgPaymentStatus.class);
    }
}
