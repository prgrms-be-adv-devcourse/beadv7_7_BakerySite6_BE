package com.openbake.cart.presentation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CartPickupDateResponse {
    private Long cartId;
    private LocalDate pickupDate;
}
