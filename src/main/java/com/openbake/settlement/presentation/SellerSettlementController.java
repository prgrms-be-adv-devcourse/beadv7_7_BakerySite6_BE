package com.openbake.settlement.presentation;

import com.openbake.common.response.ApiResponse;
import com.openbake.settlement.application.SellerSettlementDetailResult;
import com.openbake.settlement.application.SellerSettlementQueryService;
import com.openbake.settlement.application.SellerSettlementSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellers/{sellerId}/settlements")
@RequiredArgsConstructor
public class SellerSettlementController {

    private final SellerSettlementQueryService
            sellerSettlementQueryService;

    /**
     * settlementId
     * +
     * sellerId
     * 판매자 10이 판매자 20의 정산 ID를 임의로 조회하는 것을 막을 수 있음
     *
     * 인증이 완성되면 sellerId를 토큰에서 가져오도록 변경 예정
     * */
    @GetMapping
    public ApiResponse<SellerSettlementListResponse>
    getSettlements(
            @PathVariable Long sellerId
    ) {
        List<SellerSettlementSummary> summaries =
                sellerSettlementQueryService
                        .getSettlements(sellerId);

        return ApiResponse.ok(
                SellerSettlementListResponse.from(summaries)
        );
    }

    @GetMapping("/{settlementId}")
    public ApiResponse<SellerSettlementDetailResponse>
    getSettlement(
            @PathVariable Long sellerId,
            @PathVariable Long settlementId
    ) {
        SellerSettlementDetailResult result =
                sellerSettlementQueryService.getSettlement(
                        sellerId,
                        settlementId
                );

        return ApiResponse.ok(
                SellerSettlementDetailResponse.from(result)
        );
    }
}