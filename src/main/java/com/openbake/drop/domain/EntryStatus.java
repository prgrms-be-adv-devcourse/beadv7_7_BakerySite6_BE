package com.openbake.drop.domain;

import lombok.Getter;

@Getter
public enum EntryStatus {
    ENTRY("입장 대기"),
    RESERVED("진입 성공"),
    COMPLETED("결제 완료");

    private final String message;

    EntryStatus(String message) {
        this.message = message;
    }
}
