package com.openbake.settlement.application;

import org.springframework.stereotype.Service;

@Service
public class SettlementTestService {

    public String getTestMessage() {
        return "정산 API가 정상적으로 실행 중입니다.";
    }
}