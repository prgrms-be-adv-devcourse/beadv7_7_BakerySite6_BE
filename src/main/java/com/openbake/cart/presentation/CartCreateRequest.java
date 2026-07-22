package com.openbake.cart.presentation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CartCreateRequest {
    private Long dropId;
    private Integer quantity;
}