package com.openbake.payment.presentation;

import com.openbake.payment.application.ChargeService;
import com.openbake.payment.domain.ChargeRequest;
import com.openbake.payment.infrastructure.ChargeRequestRepository;
import com.openbake.payment.presentation.dto.TossWebhookRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 토스페이먼츠 웹훅 수신 컨트롤러.
 *
 * 웹훅은 "보험"이다:
 * - 정상 흐름에서는 프론트 → 서버 → PG 승인으로 처리가 완료된다.
 * - 하지만 서버가 PG 응답을 받기 전에 죽거나, 네트워크 문제로 응답을 놓치면
 *   토스가 웹훅으로 "결제 완료됐어" 알려준다.
 * - 이미 처리된 건이면 무시한다 (멱등성: ChargeRequest.markDone이 이미 DONE이면 무시).
 *
 * ARD-005: 웹훅은 동기 처리 우선. 성능 이슈 나면 @Async 전환.
 */
@Slf4j
@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final ChargeRequestRepository chargeRequestRepository;
    private final ChargeService chargeService;

    @PostMapping("/toss")
    public ResponseEntity<Void> handleTossWebhook(@RequestBody TossWebhookRequest request) {
        log.info("[웹훅 수신] eventType={}, paymentKey={}, orderId={}, status={}",
                request.eventType(), request.paymentKey(), request.orderId(), request.status());

        // DONE 상태 웹훅만 처리 (취소 등은 현재 미지원)
        if (!"DONE".equals(request.status())) {
            return ResponseEntity.ok().build();
        }

        // pgPaymentKey로 충전 요청 조회
        ChargeRequest chargeRequest = chargeRequestRepository
                .findByPgPaymentKey(request.paymentKey())
                .orElse(null);

        if (chargeRequest == null) {
            log.warn("[웹훅] 매칭되는 충전 요청 없음: paymentKey={}", request.paymentKey());
            return ResponseEntity.ok().build();
        }

        // 이미 완료된 건이면 무시 (멱등성)
        if (chargeRequest.isDone()) {
            log.info("[웹훅] 이미 처리 완료된 건: chargeRequestId={}", chargeRequest.getId());
            return ResponseEntity.ok().build();
        }

        // 아직 완료 안 된 건이면 처리
        // TODO: 웹훅에서는 method 정보가 없으므로 null 처리. 필요하면 PG 조회 API로 보완.
        chargeService.completeCharge(chargeRequest, null);
        log.info("[웹훅] 충전 완료 처리: chargeRequestId={}", chargeRequest.getId());

        // 토스 웹훅은 200 OK를 받아야 재시도를 멈춤
        return ResponseEntity.ok().build();
    }
}
