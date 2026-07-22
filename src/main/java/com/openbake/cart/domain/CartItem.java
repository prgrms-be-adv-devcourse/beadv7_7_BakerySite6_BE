package com.openbake.cart.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cart_items")
@Setter
@Getter
@NoArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long cartItemId;

    @OneToOne(fetch = FetchType.LAZY) //LAZY 지연로딩
    @JoinColumn(name = "cart_id", nullable = false, unique = true)
    private Cart cart;

    @Column(nullable = false)
    private Long dropId;

    @Column(nullable = false)
    private Integer quantity;

    //정적 팩토리 메서드
    public static CartItem create(Long dropId, Integer quantity) {
        CartItem item = new CartItem();
        item.dropId = dropId;
        item.quantity = quantity;
        return item;
    }

}
