package com.openbake.cart.presentation;

import com.openbake.cart.application.CartService;
import com.openbake.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    //경로에 cartId 가 없다. 대상 장바구니는 인증 토큰의 회원으로 특정한다.
    //JwtAuthenticationFilter 가 인증 주체에 memberId(Long) 를 넣어두므로 그대로 받는다.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CartCreateResponse> create(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CartCreateRequest request) {
        return ApiResponse.ok(cartService.create(memberId, request));
    }
}
