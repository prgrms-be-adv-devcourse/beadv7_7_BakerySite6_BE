package com.openbake.payment.presentation;

import com.openbake.common.response.ApiResponse;
import com.openbake.payment.application.ChargeFacade;
import com.openbake.payment.application.ChargeService;
import com.openbake.payment.presentation.dto.ChargeApproveRequest;
import com.openbake.payment.presentation.dto.ChargeApproveResponse;
import com.openbake.payment.presentation.dto.ChargeCreateRequest;
import com.openbake.payment.presentation.dto.ChargeCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/charges")
@RequiredArgsConstructor
public class ChargeController {

    private final ChargeService chargeService;
    private final ChargeFacade chargeFacade;

    /**
     * 충전 요청 생성.
     * 프론트는 이 응답의 pgOrderId와 amount로 토스페이먼츠 결제창을 띄운다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ChargeCreateResponse>> createCharge(
            @RequestBody ChargeCreateRequest request) {
        ChargeCreateResponse response = chargeService.createChargeRequest(
                request.memberId(), request.amount());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 충전 승인.
     * 프론트가 토스 결제창 완료 후 paymentKey, orderId, amount를 보내면
     * 서버가 PG 승인 API를 호출하고 예치금을 증가시킨다.
     */
    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<ChargeApproveResponse>> approveCharge(
            @RequestBody ChargeApproveRequest request) {
        ChargeApproveResponse response = chargeFacade.approve(
                request.memberId(), request.pgPaymentKey(),
                request.pgOrderId(), request.amount());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
