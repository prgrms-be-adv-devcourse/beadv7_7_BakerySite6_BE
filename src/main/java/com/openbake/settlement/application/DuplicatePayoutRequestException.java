package com.openbake.settlement.application;

import com.openbake.common.exception.BusinessException;
import com.openbake.common.exception.ErrorCode;

/**
 * 중복 멱등키 ErrorCode 추가
 * BusinessException을 상속한 전용 예외를 생성
 * */
public class DuplicatePayoutRequestException
        extends BusinessException {

    public DuplicatePayoutRequestException(
            String message
    ) {
        super(
                ErrorCode.DUPLICATE_PAYOUT_REQUEST,
                message
        );
    }
}