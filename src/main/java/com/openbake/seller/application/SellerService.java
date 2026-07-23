package com.openbake.seller.application;

import com.openbake.common.exception.BusinessVerificationFailedException;
import com.openbake.seller.infrastructure.MockBusinessRegistry;
import com.openbake.seller.presentation.dto.BusinessVerificationRequest;
import com.openbake.seller.presentation.dto.BusinessVerificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final MockBusinessRegistry mockBusinessRegistry;

    public BusinessVerificationResponse verifyBusiness(BusinessVerificationRequest request) {
        if (!mockBusinessRegistry.isRegistered(request.businessNumber(), request.businessRepresentativeName())) {
            throw new BusinessVerificationFailedException();
        }

        return new BusinessVerificationResponse(true, request.businessNumber(), LocalDateTime.now());
    }
}
