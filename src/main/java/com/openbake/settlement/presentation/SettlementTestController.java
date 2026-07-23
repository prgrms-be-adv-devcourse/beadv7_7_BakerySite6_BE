package com.openbake.settlement.presentation;

import com.openbake.settlement.application.SettlementTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/settlements")
public class SettlementTestController {

    private final SettlementTestService settlementTestService;

    @GetMapping("/test")
    public ResponseEntity<SettlementTestResponse> test() {
        String message = settlementTestService.getTestMessage();

        return ResponseEntity.ok(
                SettlementTestResponse.success(message)
        );
    }
}