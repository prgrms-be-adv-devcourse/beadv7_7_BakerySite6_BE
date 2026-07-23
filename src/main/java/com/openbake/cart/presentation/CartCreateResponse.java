package com.openbake.cart.presentation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CartCreateResponse {
    private Long cartId;
    private Long dropId;
    private Integer quantity;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}