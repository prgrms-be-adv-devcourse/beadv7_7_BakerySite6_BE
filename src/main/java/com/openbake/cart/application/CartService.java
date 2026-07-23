package com.openbake.cart.application;

import com.openbake.cart.domain.Cart;
import com.openbake.cart.domain.CartItem;
import com.openbake.cart.domain.CartRepository;
import com.openbake.cart.presentation.CartCreateRequest;
import com.openbake.cart.presentation.CartCreateResponse;
import com.openbake.common.exception.BusinessException;
import com.openbake.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    //만료 시간은 정책값이라 코드에 박지 않고 설정에서 받는다.
    @Value("${openbake.cart.ttl}")
    private Duration cartTtl;

    /**
     * 장바구니 생성. 재고를 선점하는 지점이다.
     * 주문 생성은 이미 선점된 재고를 확정만 하므로 재고를 깎는 곳은 여기 한 곳이다.
     */
    @Transactional
    public CartCreateResponse create(Long memberId, CartCreateRequest request) {
        //만료되지 않은 장바구니가 이미 있으면 새로 담을 수 없다.
        if (cartRepository.existsByMemberId(memberId)) {
            throw new BusinessException(ErrorCode.CART_ALREADY_EXISTS);
        }

        // TODO(drop): 드롭 존재 여부 · 판매 기간 · 드롭 상태 검증
        //   없으면 DROP_NOT_FOUND(404), 오픈 전이거나 종료됐으면 DROP_NOT_ON_SALE(409)

        // TODO(drop): 1인 구매 제한 검증 — 초과 시 PER_PERSON_LIMIT_EXCEEDED(409)
        //   drop_meta_data.limit_quantity 와 비교한다. 기존 유효 주문 수량 합산은 order 도메인 완성 후.

        // TODO(drop): 재고 선점 — 분산락 + 조건부 UPDATE
        //   UPDATE drop_inventories
        //      SET available_quantity = available_quantity - :quantity
        //    WHERE drop_id = :dropId AND available_quantity >= :quantity
        //   갱신 행 수가 0이면 SOLD_OUT(409).
        //   조회 후 차감하면 두 요청이 모두 통과해 오버셀이 나므로 쓰지 않는다.
        //   ERD 에서 drop_inventories.version 이 삭제돼 낙관적 락이 없으므로 이 쿼리가 최종 방어선이다.

        LocalDateTime now = LocalDateTime.now();
        Cart cart = Cart.create(memberId, now.plus(cartTtl));
        cart.addItem(CartItem.create(request.getDropId(), request.getQuantity()));

        Cart saved;
        try {
            //saveAndFlush 로 즉시 INSERT 해야 UNIQUE 위반을 이 자리에서 잡을 수 있다.
            //save 만 쓰면 커밋 시점에 flush 돼 try 밖에서 터진다.
            saved = cartRepository.saveAndFlush(cart);
        } catch (DataIntegrityViolationException e) {
            //carts.member_id UNIQUE 위반.
            //더블클릭 등으로 두 요청이 위 existsByMemberId 검사를 함께 통과한 경우다.
            //선검사만으로는 동시 요청을 막을 수 없어 DB 제약이 최종 방어선이 된다.
            throw new BusinessException(ErrorCode.CART_ALREADY_EXISTS);
        }

        return CartCreateResponse.builder()
                .cartId(saved.getCartId())
                .dropId(request.getDropId())
                .quantity(request.getQuantity())
                .expiresAt(saved.getExpiresAt())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
