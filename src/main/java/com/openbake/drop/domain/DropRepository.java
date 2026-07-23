package com.openbake.drop.domain;


import java.time.LocalDateTime;

public interface DropRepository {
    Drop save(Drop drop);

    // 해당 판매자가 해당 날짜(00:00:00 ~ 23:59:59)에 이미 등록한 드롭이 있는지 확인
    boolean existsBySellerIdAndDropStartBetween(
            Long sellerId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );
}
