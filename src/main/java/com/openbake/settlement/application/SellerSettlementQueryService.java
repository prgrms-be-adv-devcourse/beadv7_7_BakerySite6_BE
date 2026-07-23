package com.openbake.settlement.application;

import com.openbake.common.exception.EntityNotFoundException;
import com.openbake.settlement.domain.Settlement;
import com.openbake.settlement.domain.SettlementLine;
import com.openbake.settlement.domain.SettlementLineRepository;
import com.openbake.settlement.domain.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerSettlementQueryService {

    private final SettlementRepository settlementRepository;
    private final SettlementLineRepository settlementLineRepository;

    public List<SellerSettlementSummary> getSettlements(
            Long sellerId
    ) {
        validatePositiveId(sellerId, "sellerId");

        return settlementRepository.findAllBySellerId(sellerId)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public SellerSettlementDetailResult getSettlement(
            Long sellerId,
            Long settlementId
    ) {
        validatePositiveId(sellerId, "sellerId");
        validatePositiveId(settlementId, "settlementId");

        Settlement settlement =
                settlementRepository
                        .findByIdAndSellerId(
                                settlementId,
                                sellerId
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "판매자의 정산 정보를 찾을 수 없습니다. "
                                                + "sellerId=" + sellerId
                                                + ", settlementId="
                                                + settlementId
                                )
                        );

        List<SettlementLine> settlementLines =
                settlementLineRepository
                        .findAllBySettlementId(settlementId);

        List<SellerSettlementDetailResult.Line> lines =
                settlementLines.stream()
                        .map(this::toDetailLine)
                        .toList();

        return new SellerSettlementDetailResult(
                settlement.getId(),
                settlement.getSellerId(),
                settlement.getPeriodStart(),
                settlement.getPeriodEnd(),
                settlement.getGrossSalesAmount(),
                settlement.getCommissionAmount(),
                settlement.getNetSalesAmount(),
                settlement.getAdjustmentAmount(),
                settlement.getPayoutAmount(),
                settlement.getTargetCount(),
                settlement.getStatus().name(),
                settlement.getCreatedAt(),
                settlement.getCompletedAt(),
                lines
        );
    }

    private SellerSettlementSummary toSummary(
            Settlement settlement
    ) {
        return new SellerSettlementSummary(
                settlement.getId(),
                settlement.getPeriodStart(),
                settlement.getPeriodEnd(),
                settlement.getGrossSalesAmount(),
                settlement.getCommissionAmount(),
                settlement.getAdjustmentAmount(),
                settlement.getPayoutAmount(),
                settlement.getTargetCount(),
                settlement.getStatus().name(),
                settlement.getCreatedAt(),
                settlement.getCompletedAt()
        );
    }

    private SellerSettlementDetailResult.Line toDetailLine(
            SettlementLine line
    ) {
        return new SellerSettlementDetailResult.Line(
                line.getId(),
                line.getTargetId(),
                line.getOrderId(),
                line.getOrderItemId(),
                line.getDropId(),
                line.getProductNameSnapshot(),
                line.getQuantity(),
                line.getGrossAmount(),
                line.getCommissionRateSnapshot(),
                line.getCommissionAmount(),
                line.getNetAmount(),
                line.getPurchaseConfirmedAt()
        );
    }

    private void validatePositiveId(
            Long value,
            String fieldName
    ) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(
                    fieldName + "는 0보다 커야 합니다."
            );
        }
    }
}