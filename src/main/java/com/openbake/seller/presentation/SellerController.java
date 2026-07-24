package com.openbake.seller.presentation;

import com.openbake.common.response.ApiResponse;
import com.openbake.seller.application.SellerService;
import com.openbake.seller.presentation.dto.AccountVerificationConfirmRequest;
import com.openbake.seller.presentation.dto.AccountVerificationConfirmResponse;
import com.openbake.seller.presentation.dto.AccountVerificationStartRequest;
import com.openbake.seller.presentation.dto.AccountVerificationStartResponse;
import com.openbake.seller.presentation.dto.BusinessVerificationRequest;
import com.openbake.seller.presentation.dto.BusinessVerificationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/business-verifications")
    public ApiResponse<BusinessVerificationResponse> verifyBusiness(@Valid @RequestBody BusinessVerificationRequest request) {
        return ApiResponse.ok(sellerService.verifyBusiness(request));
    }

    @PostMapping("/settlement-account/verification-requests")
    public ApiResponse<AccountVerificationStartResponse> requestAccountVerification(
            @Valid @RequestBody AccountVerificationStartRequest request) {
        return ApiResponse.ok(sellerService.requestAccountVerification(request));
    }

    @PostMapping("/settlement-account/verification-requests/{verificationRequestId}/verify")
    public ApiResponse<AccountVerificationConfirmResponse> verifyAccount(
            @PathVariable String verificationRequestId,
            @Valid @RequestBody AccountVerificationConfirmRequest request) {
        return ApiResponse.ok(sellerService.verifyAccount(verificationRequestId, request));
    }

}
