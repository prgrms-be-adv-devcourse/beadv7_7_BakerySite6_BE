package com.openbake.drop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DropProduct { // 상품 VO 객체

    @Column(nullable = false)
    private String name; // 상품 이름

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description; // 상품 설명

    @Column(columnDefinition = "TEXT", nullable = false)
    private String imageUrl; // 이미지 경로 (DB 백업 크기 줄일 수 있고 로드 분산 가능, BLOB으로 저장 시 파일과 DB 간 일관성 관리 쉽고 동기화 보장 but DB 용량 급증)

    @Column(nullable = false)
    private int price; // 상품 가격

    @Builder
    public DropProduct(String name, String description, String imageUrl, int price) {
        this.name=name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
    }
}
