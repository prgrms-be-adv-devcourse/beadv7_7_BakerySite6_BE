package com.openbake.drop.domain;

import lombok.Getter;

@Getter
public enum DropStatus { // Drop 상태 값
    UPCOMING("시작 전"),
    ACTIVE("진행중"),
    COMPLETED("마감");

    private final String message;

    DropStatus(String message) {
        this.message = message;
    }
}
