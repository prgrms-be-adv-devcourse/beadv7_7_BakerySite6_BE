package com.openbake.settlement.infrastructure.batch;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 정산 배치 스케줄링을 활성화합니다.
 * @EnableScheduling이 있어야 Spring이 @Scheduled 메서드를 찾아 예약 실행
 */
@Configuration
@EnableScheduling
public class SettlementSchedulingConfig {
}