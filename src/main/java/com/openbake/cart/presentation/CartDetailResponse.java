package com.openbake.cart.presentation;

import com.openbake.cart.domain.Cart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CartDetailResponse {
    private Long cartId;
    private DropInfo drop;
    private SellerInfo seller;
    private Integer quantity;
    private BigDecimal estimatedAmount;
    private List<LocalDate> pickupDates;
    private LocalDate selectedPickupDate;
    private LocalDateTime expiresAt;
    private Integer remainingSeconds;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DropInfo{
        private Long dropId;
        private String dropName;
        private BigDecimal price;
        private String imageUrl;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SellerInfo{
        private Long sellerId;
        private String sellerName;
    }
}
