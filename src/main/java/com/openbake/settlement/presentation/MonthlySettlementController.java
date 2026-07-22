package com.openbake.settlement.presentation;

import com.openbake.settlement.application.MonthlySettlementResult;
import com.openbake.settlement.application.MonthlySettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/settlements")
@RequiredArgsConstructor
public class MonthlySettlementController {

    private final MonthlySettlementService monthlySettlementService;

    /**
     * 지정된 기간의 정산 대상을 판매자별로 월 정산합니다.
     *
     * 현재는 관리자 또는 배치 검증용 내부 API입니다.
     */
    @PostMapping("/monthly")
    public ResponseEntity<MonthlySettlementResponse> settleMonthly(
            @RequestBody MonthlySettlementRequest request
    ) {
        MonthlySettlementResult result =
                monthlySettlementService.settle(
                        request.periodStart(),
                        request.periodEnd()
                );

        return ResponseEntity.ok(
                MonthlySettlementResponse.from(result)
        );
    }
}