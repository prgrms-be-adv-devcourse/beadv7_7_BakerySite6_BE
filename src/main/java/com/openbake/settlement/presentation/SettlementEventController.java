package com.openbake.settlement.presentation;

import com.openbake.settlement.application.SettlementEventResult;
import com.openbake.settlement.application.SettlementEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 정산 이벤트를 수신하는 내부 API 컨트롤러입니다.
 *
 * 현재는 주문 도메인에서 전달하는 구매확정 이벤트를 처리합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/settlement-events")
public class SettlementEventController {

    private final SettlementEventService settlementEventService;

    /**
     * 구매확정 이벤트를 수신하여 정산 대상을 생성합니다.
     *
     * 새로운 정산 대상이 생성되면 201 Created,
     * 이미 처리한 이벤트라면 200 OK를 반환합니다.
     */
    @PostMapping("/purchase-confirmed")
    public ResponseEntity<SettlementEventResponse> receivePurchaseConfirmed(
            @RequestBody PurchaseConfirmedRequest request
    ) {
        SettlementEventResult result =
                settlementEventService.receive(
                        request.toCommand()
                );

        SettlementEventResponse response =
                SettlementEventResponse.from(result);

        if (result.duplicate()) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}