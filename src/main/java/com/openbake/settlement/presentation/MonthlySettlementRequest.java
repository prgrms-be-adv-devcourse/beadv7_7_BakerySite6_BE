package com.openbake.settlement.presentation;

import java.time.LocalDate;

/**
 * 월 정산 실행 요청입니다.
 *
 * periodStart는 포함하고 periodEnd는 포함하지 않습니다.
 *
 * 예:
 * 2026-07-01 이상
 * 2026-08-01 미만
 */
public record MonthlySettlementRequest(
        LocalDate periodStart,
        LocalDate periodEnd
) {
}