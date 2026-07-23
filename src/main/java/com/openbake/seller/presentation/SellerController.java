package com.openbake.seller.presentation;

import com.openbake.common.response.ApiResponse;
import com.openbake.seller.application.SellerService;
import com.openbake.seller.presentation.dto.BusinessVerificationRequest;
import com.openbake.seller.presentation.dto.BusinessVerificationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

}
